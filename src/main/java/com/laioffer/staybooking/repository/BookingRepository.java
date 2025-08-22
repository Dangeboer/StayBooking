package com.laioffer.staybooking.repository;

import com.laioffer.staybooking.model.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findAllByGuestId(long guestId);

    List<BookingEntity> findAllByListingId(long listingId);

    // 返回所有有冲突的预定信息是否有重复的时间
    // 这里的 :listingId、:checkOut 和 :checkIn 是占位符，它们不会直接被解析，而是在执行查询时由代码传入实际的值。
    @Query(value = "SELECT * FROM bookings WHERE listing_id = :listingId AND check_in_date < :checkOut AND check_out_date > :checkIn", nativeQuery = true)
    List<BookingEntity> findOverlappedBookings(long listingId, LocalDate checkIn, LocalDate checkOut);

    boolean existsByListingIdAndCheckOutDateAfter(long listingId, LocalDate date);
}