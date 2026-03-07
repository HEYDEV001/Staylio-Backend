package com.Backend.Projects.AirBnb.service;


import com.Backend.Projects.AirBnb.entities.Booking;
import com.Backend.Projects.AirBnb.entities.User;
import com.Backend.Projects.AirBnb.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckOutServiceImpl implements CheckOutService {

    private final BookingRepository bookingRepository;

    @Override
    public String getCheckOutSession(Booking booking, String successUrl, String failureUrl) {

        log.info("Trying to create checkout session for bookingId {}", booking.getId());
        log.info("Trying to get the User form the SecurityContextHolder");
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        try {

            log.info("Trying to create Customer ");
            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setName(user.getName())
                    .setEmail(user.getEmail())
                    .build();
            Customer customer = Customer.create(customerParams);

            log.info("Trying to create Session");
            SessionCreateParams sessionParams = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .setCustomer(customer.getId())
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(failureUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("INR")
                                                    .setUnitAmount(booking.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(booking.getHotel().getName() + ":" + booking.getRoom().getType())
                                                                    .setDescription("Booking Id: " + booking.getId())
                                                                    .build()
                                                    )
                                                    .build())
                                    .build())
                    .build();

            Session session = Session.create(sessionParams);

            log.info("Saving SessionId in the booking with bookingId {}", booking.getId());
            booking.setPaymentSessionId(session.getId());
            bookingRepository.save(booking);

            return session.getId();

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }
}
