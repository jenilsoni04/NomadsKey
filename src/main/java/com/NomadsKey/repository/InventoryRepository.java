package com.NomadsKey.repository;

import com.NomadsKey.dto.HotelPriceDto;
import com.NomadsKey.dto.RoomPriceDto;
import com.NomadsKey.entity.Hotel;
import com.NomadsKey.entity.Inventory;
import com.NomadsKey.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    void deleteByRoom(Room room);

    @Query("""
            SELECT new com.NomadsKey.dto.HotelPriceDto(i.hotel, AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.hotel.city = :city
                AND i.date BETWEEN :startDate AND :endDate
                AND i.hotel.active = true
           GROUP BY i.hotel
           """)
    Page<HotelPriceDto> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );


            @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND i.closed = false
                AND (i.totalCount - i.bookCount - i.ReservedCount) >= :roomsCount
            """)

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findandlockavailableInventory(
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
                  AND (i.totalCount - i.bookCount) >= :numberOfRooms
                  AND i.closed = false
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockReservedInventory(@Param("roomId") Long roomId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("numberOfRooms") int numberOfRooms);

    List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);

    @Modifying
    @Query("""
                UPDATE Inventory i
                SET i.ReservedCount = i.ReservedCount + :numberOfRooms
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
                  AND (i.totalCount - i.bookCount - i.ReservedCount) >= :numberOfRooms
                  AND i.closed = false
            """)
    void initBooking(@Param("roomId") Long roomId,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate,
                     @Param("numberOfRooms") int numberOfRooms);

    @Modifying
    @Query("""
                UPDATE Inventory i
                SET i.ReservedCount = i.ReservedCount - :numberOfRooms,
                    i.bookCount = i.bookCount + :numberOfRooms
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
                  AND (i.totalCount - i.bookCount) >= :numberOfRooms
                  AND i.ReservedCount >= :numberOfRooms
                  AND i.closed = false
            """)
    void confirmBooking(@Param("roomId") Long roomId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("numberOfRooms") int numberOfRooms);

    @Modifying
    @Query("""
                UPDATE Inventory i
                SET i.bookCount = i.bookCount - :numberOfRooms
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
                  AND (i.totalCount - i.bookCount) >= :numberOfRooms
                  AND i.closed = false
            """)
    void cancelBooking(@Param("roomId") Long roomId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate,
                       @Param("numberOfRooms") int numberOfRooms);

    Collection<Object> findByRoomOrderByDate(Room room);

    @Query("""
                SELECT i
                FROM Inventory i
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> getInventoryAndLockBeforeUpdate(@Param("roomId") Long roomId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    @Modifying
    @Query("""
                UPDATE Inventory i
                SET i.surgeFactor = :surgeFactor,
                    i.closed = :closed
                WHERE i.room.id = :roomId
                  AND i.date BETWEEN :startDate AND :endDate
            """)
    void updateInventory(@Param("roomId") Long roomId,
                         @Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate,
                         @Param("closed") boolean closed,
                         @Param("surgeFactor") BigDecimal surgeFactor);

    @Query("""
       SELECT new com.NomadsKey.dto.RoomPriceDto(
            i.room,
            CASE
                WHEN COUNT(i) = :dateCount THEN CAST(AVG(i.price) AS double)                  
                ELSE NULL
            END
        )
       FROM Inventory i
       WHERE i.hotel.id = :hotelId
             AND i.date BETWEEN :startDate AND :endDate
             AND (i.totalCount - i.bookCount) >= :roomsCount
             AND i.closed = false
       GROUP BY i.room
       """)
    default List<RoomPriceDto> findRoomAveragePrice(
            @Param("hotelId") Long hotelId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Long roomsCount,
            @Param("dateCount") Long dateCount
    ) {
        return null;
    }
}
