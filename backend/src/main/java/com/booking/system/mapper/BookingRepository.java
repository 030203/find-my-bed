package com.booking.system.mapper;

import com.booking.system.entity.Booking;
import com.booking.system.entity.Property;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingNumber(String bookingNumber);
    List<Booking> findByUser_Id(Long userId);
    List<Booking> findByProperty_Id(Long propertyId);
    List<Booking> findByProperty_MerchantId(Long merchantId);
    List<Booking> findByStatus(Booking.BookingStatus status);
    List<Booking> findByPaymentStatus(Booking.PaymentStatus paymentStatus);

    @Query("select b.property.id, count(b) from Booking b where b.property.status = :status group by b.property.id order by count(b) desc")
    List<Object[]> findTopPropertyBookingCounts(@Param("status") Property.PropertyStatus status, Pageable pageable);

    @Query("""
            select count(b) from Booking b
            where b.roomType.id = :roomTypeId
              and (:excludeBookingId is null or b.id <> :excludeBookingId)
              and b.status in :statuses
              and b.checkInDate < :checkOutDate
              and b.checkOutDate > :checkInDate
            """)
    long countConflictingBookings(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("statuses") Set<Booking.BookingStatus> statuses,
            @Param("excludeBookingId") Long excludeBookingId
    );

    @Query("""
            select count(b) from Booking b
            where b.room.id = :roomId
              and (:excludeBookingId is null or b.id <> :excludeBookingId)
              and b.status in :statuses
              and b.checkInDate < :checkOutDate
              and b.checkOutDate > :checkInDate
            """)
    long countConflictingBookingsByRoomId(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("statuses") Set<Booking.BookingStatus> statuses,
            @Param("excludeBookingId") Long excludeBookingId
    );
}
