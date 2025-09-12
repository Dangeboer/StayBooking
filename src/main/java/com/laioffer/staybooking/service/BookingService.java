package com.laioffer.staybooking.service;

import com.laioffer.staybooking.exception.DeleteBookingNotAllowedException;
import com.laioffer.staybooking.exception.InvalidBookingException;
import com.laioffer.staybooking.exception.ListingBookingsNotAllowedException;
import com.laioffer.staybooking.model.dto.BookingDto;
import com.laioffer.staybooking.model.entity.BookingEntity;
import com.laioffer.staybooking.model.entity.ListingEntity;
import com.laioffer.staybooking.repository.BookingRepository;
import com.laioffer.staybooking.repository.ListingRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;
    private final RedissonClient redissonClient;

    public BookingService(
            BookingRepository bookingRepository,
            ListingRepository listingRepository,
            RedissonClient redissonClient
    ) {
        this.bookingRepository = bookingRepository;
        this.listingRepository = listingRepository;
        this.redissonClient = redissonClient;
    }

    // 通过住户id找预定信息，把返回的entity转换为dto，再返回；保证隐私数据的不泄漏
    public List<BookingDto> findBookingsByGuestId(long guestId) {
        return bookingRepository.findAllByGuestId(guestId)
                // 数据转换，从BookingEntity转为BookingDto
                .stream()
                .map(BookingDto::new)
                .toList();
    }

    // 通过房源id找到预定信息：先检查用户（房东）是否是该房源的房东，如果是，就通过房源id找到预定信息
    public List<BookingDto> findBookingsByListingId(long hostId, long listingId) {
        ListingEntity listing = listingRepository.getReferenceById(listingId);
        if (listing.getHostId() != hostId) {
            throw new ListingBookingsNotAllowedException(hostId, listingId);
        }
        return bookingRepository.findAllByListingId(listingId)
                .stream()
                .map(BookingDto::new)
                .toList();
    }

    // 通过用户id，房源id，入住、退房日期来创建新的预定信息
    @Transactional
    public void createBooking(long guestId, long listingId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isAfter(checkOut)) { // 入住日期不能晚于退房日期
            throw new InvalidBookingException("Check-in date must be before check-out date.");
        }
        if (checkIn.isBefore(LocalDate.now())) { // 入住时间不能早于当前日期
            throw new InvalidBookingException("Check-in date must be in the future.");
        }

        // 使用 Redisson 分布式锁替代数据库锁
        String lockKey = "booking:lock:listing:" + listingId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 尝试获取锁，最多等待10秒，锁持有时间最多30秒
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                try {
                    // 1. 检查房源是否存在
                    ListingEntity listing = listingRepository.findById(listingId)
                            .orElseThrow(() -> new InvalidBookingException("Listing not found"));

                    // 2. 查询当前房源是否在当前日期内，已经被预定，有冲突
                    List<BookingEntity> overlappedBookings = bookingRepository.findOverlappedBookings(listingId, checkIn, checkOut);
                    if (!overlappedBookings.isEmpty()) {
                        throw new InvalidBookingException("Booking dates conflict, please select different dates.");
                    }

                    // 3. 保存 booking 信息
                    bookingRepository.save(new BookingEntity(null, guestId, listingId, checkIn, checkOut));
                } finally {
                    // 确保在方法结束时释放锁
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                throw new InvalidBookingException("Unable to acquire lock for booking. Please try again.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidBookingException("Booking process was interrupted. Please try again.");
        }
    }

    // 删除某个预定关系：先通过预定id找到预定信息，如果当前住户id符合预定信息里的住户id，就允许删除
    public void deleteBooking(long guestId, long bookingId) {
        BookingEntity booking = bookingRepository.getReferenceById(bookingId);
        // 如果要删的这个用户和数据库中存储的当前预定的用户不同，就抛异常
        if (booking.getGuestId() != guestId) {
            throw new DeleteBookingNotAllowedException(guestId, bookingId);
        }
        bookingRepository.deleteById(bookingId);
    }

    public boolean existsActiveBookings(long listingId) {
        return bookingRepository.existsByListingIdAndCheckOutDateAfter(listingId, LocalDate.now());
    }
}
