package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.dto.BookingDto;
import com.Backend.Projects.AirBnb.dto.BookingRequest;
import com.Backend.Projects.AirBnb.dto.GuestDto;
import com.Backend.Projects.AirBnb.dto.HotelReportDto;
import com.Backend.Projects.AirBnb.entities.*;
import com.Backend.Projects.AirBnb.entities.enums.BookingStatus;
import com.Backend.Projects.AirBnb.exceptions.ResourceNotFoundException;
import com.Backend.Projects.AirBnb.exceptions.UnAuthorizedException;
import com.Backend.Projects.AirBnb.repository.*;
import com.Backend.Projects.AirBnb.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.Backend.Projects.AirBnb.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {


    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final CheckOutService checkOutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontEndUrl;

    @Override
    @Transactional
    public BookingDto initializeBooking(BookingRequest bookingRequest) {
        log.info("Initializing Booking for the Hotel with id {} from {} to {} ", bookingRequest.getHotelId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        Hotel hotel = hotelRepository
                .findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id : " + bookingRequest.getRoomId()));

        log.info("Getting and locking the available  Inventory");
        List<Inventory> inventoryList = inventoryRepository.getAndLockAvailableInventory(
                room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate()
        ) + 1;

        if (inventoryList.size() != daysCount) {
            throw new IllegalArgumentException("Room are not available Anymore ");
        }

        inventoryRepository.initBooking(room.getId(),
                bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        BigDecimal priceOfOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceOfOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));



        log.info("creating the booking");
        Booking booking = Booking.builder()
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .roomCount(bookingRequest.getRoomsCount())
                .createdAt(LocalDateTime.now())
                .user(getCurrentUser())
                .status(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .amount(totalPrice)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        return modelMapper.map(savedBooking, BookingDto.class);

    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding the guests for the Booking with Id {} ", bookingId);

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id : " + bookingId));

        User currentUser = getCurrentUser();

        if (!currentUser.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to the current user with the id : " + currentUser.getId());
        }

        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking expired");
        }
        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("Booking is not reserved, cannot add guests");
        }

        for (GuestDto guestDto : guestDtoList) {
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(currentUser);
            guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setStatus(BookingStatus.GUEST_ADDED);
        Booking savedBooking = bookingRepository.save(booking);
        return modelMapper.map(savedBooking, BookingDto.class);

    }

    @Override
    @Transactional
    public String initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id : " + bookingId));
        User currentUser = getCurrentUser();
        if (!currentUser.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to the current user with the id : " + currentUser.getId());
        }
        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking expired");
        }

        String sessionUrl = checkOutService.getCheckOutSession(booking, frontEndUrl + "/payments/success", frontEndUrl + "/payments/failure");

        booking.setStatus(BookingStatus.PAYMENTS_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;

    }

    @Override
    @Transactional
    public void capturePayment(Event event) {

        if ("checkout.session.completed".equals(event.getType())) {

            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;

            String sessionId = session.getId();
            Booking booking = bookingRepository.findByPaymentSessionId(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking with this payment SessionId: " + sessionId + " not found"));
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            inventoryRepository.findAndLockReservedInventory( booking.getRoom().getId(),
                    booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(),
                    booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomCount());
            log.info("Successfully confirmed the booking for bookingId {} ", booking.getId());

        } else {
            log.warn("Unhandled event Type {} ", event.getType());
        }
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id : " + bookingId));
        User currentUser = getCurrentUser();
        if (!currentUser.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to the current user with the id : " + currentUser.getId());
        }
       if(booking.getStatus() != BookingStatus.CONFIRMED) {
           throw new IllegalStateException("Booking is not confirmed yet so it cannot be cancelled");
       }
       booking.setStatus(BookingStatus.CANCELLED);
       bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory( booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomCount());


        // Here we have created the refund logic. We can add more features like refund reason, partial refund etc.
        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundCreateParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundCreateParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }



    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {
        log.info("Getting the hotel with HotelId {}",hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id : " + hotelId));
        log.info("Getting the Current User");
        User currentUser = getCurrentUser();
        if(!currentUser.equals(hotel.getOwner())){
           throw new UnAuthorizedException("You are not allowed to access this hotel with the id : " + hotelId);
        }
        log.info("Getting all the bookings of the hotel with Id {}", hotelId);
        List<Booking> bookings = bookingRepository.findByHotel(hotel);
        return bookings.stream()
                .map((booking) -> modelMapper.map(booking, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id : " + bookingId));
        User currentUser = getCurrentUser();
        if (!currentUser.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to the current user with the id : " + currentUser.getId());
        }
        return booking.getStatus().name();
    }


    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id : " + hotelId));

        User currentUser = getCurrentUser();
        log.info("Getting hotel report for hotel with Id {}", hotelId);
        if(!currentUser.equals(hotel.getOwner())){
            throw new UnAuthorizedException("You are not allowed to access this hotel with the id : " + hotelId);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDateTime, endDateTime );

        Long totalConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        BigDecimal averageRevenue = totalConfirmedBookings == 0 ? BigDecimal.ZERO
                        : totalRevenueOfConfirmedBookings.divide( BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBookings, totalRevenueOfConfirmedBookings, averageRevenue);
    }

    @Override
    public List<BookingDto> getMyBooking() {

        User user  = getCurrentUser();

        List<Booking> bookings = bookingRepository.findByUser(user);
        return bookings.stream()
                .map((booking) -> modelMapper.map(booking, BookingDto.class))
                .collect(Collectors.toList());

    }


    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

}
