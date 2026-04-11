package com.Backend.Projects.AirBnb.service;

import com.Backend.Projects.AirBnb.entities.Hotel;
import com.Backend.Projects.AirBnb.entities.HotelMinPrice;
import com.Backend.Projects.AirBnb.entities.Inventory;
import com.Backend.Projects.AirBnb.repository.HotelMinPriceRepository;
import com.Backend.Projects.AirBnb.repository.HotelRepository;
import com.Backend.Projects.AirBnb.repository.InventoryRepository;
import com.Backend.Projects.AirBnb.strategy.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

// Scheduler to update the Inventory and HotelMinPrice Table every one hour


    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final HotelRepository hotelRepository;
    private final PricingService pricingService ;


//    @Scheduled(cron = "*/5 * * * * *")
    @Scheduled(cron = "0 0 * * * *")
    public void updatePricing(){
        int page = 0;
        int batchSize = 100;

        while(true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));
            if(hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrice);
            page++;
        }

    }

    public void updateHotelPrice(Hotel hotel){

        log.info("Updating Hotel prices for the hotel with Id {} ",hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate =LocalDate.now().plusYears(1);

        List<Inventory> inventoryList= inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);

        updateInventoryPrices(inventoryList);
        updateHotelMinPrices(hotel, inventoryList, startDate, endDate);

    }

    private void updateHotelMinPrices(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
                )).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));

        List<HotelMinPrice> hotelPrices  = new ArrayList<>();
        dailyMinPrices.forEach((date, price)->{
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel, date)
                    .orElse(new HotelMinPrice(hotel, date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        });

        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList) {

        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice  = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });

        inventoryRepository.saveAll(inventoryList);

    }

}
