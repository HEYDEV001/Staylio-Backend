package com.Backend.Projects.AirBnb.repository;

import com.Backend.Projects.AirBnb.entities.Hotel;
import com.Backend.Projects.AirBnb.entities.Inventory;
import com.Backend.Projects.AirBnb.entities.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    void deleteByRoom(Room room);

    boolean findByHotelAndRoomAndDate(Hotel hotel, Room room, LocalDate date);

    @Query("""
            SELECT DISTINCT i.hotel
            FROM Inventory i
            WHERE i.city = :city
                AND i.date BETWEEN :startDate AND :endDate
                AND i.closed =false
                AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
            GROUP BY i.hotel, i.room
            HAVING COUNT(i.date)=:dateCount
            """)
    Page<Hotel> findHotelsWithAvailableInventories(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable) ;


    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND i.closed =false
                AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> getAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );


    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND (i.totalCount - i.bookedCount) >= :roomsCount
                AND i.closed =false
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockReservedInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") int roomsCount
    );




    @Modifying
    @Query(""" 
            UPDATE Inventory i
            SET i.reservedCount = i.reservedCount + :roomsCount
            where i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
              AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
              AND i.closed =false
            """)
    void initBooking(@Param("roomId") Long roomId,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate,
                     @Param("roomsCount") int roomsCount);


    @Modifying
    @Query(""" 
            UPDATE Inventory i
            SET i.reservedCount = i.reservedCount - :roomsCount,
                i.bookedCount = i.bookedCount + :roomsCount
            where i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
              AND (i.totalCount - i.bookedCount) >= :roomsCount
              AND i.reservedCount >= :roomsCount
              AND i.closed =false
            """)
    void confirmBooking(@Param("roomId") Long roomId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("roomsCount") int roomsCount
                        );


    @Modifying
    @Query(""" 
            UPDATE Inventory i
            SET i.bookedCount = i.bookedCount - :roomsCount
            where i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
              AND (i.totalCount - i.bookedCount) >= :roomsCount
              AND i.closed =false
            """)
    void cancelBooking(@Param("roomId") Long roomId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate,
                       @Param("roomsCount") int roomsCount);


    List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);


    List<Inventory> findByRoomOrderByDate(Room room);

    @Modifying
    @Query(""" 
            UPDATE Inventory i
            SET i.surgeFactor = :surgeFactor,
                i.closed= :closed
            where i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
            """)
    void updateInventory(@Param("roomId") Long roomId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate,
                       @Param("closed") boolean closed,
                       @Param("surgeFactor") BigDecimal surgeFactor
                         );

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> selectInventoryAndLockBeforeUpdate(@Param("roomId") Long roomId,
                         @Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate
    );
}
