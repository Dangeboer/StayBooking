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
import java.util.ArrayList;
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

        // Redisson 锁住的不是某个对象，而是 Redis 里的这个 lockKey
        // 因为加了 listingId，所以锁的粒度是房源（listing）级别，不同的房源是不同的锁，不同房源之间可以并行操作
        // 同时对每一天加锁，从checkIn的当天到checkOut的前一天，不同的时间段允许同时预定
        // 这个锁是分布式的，意味着即使你有多台服务同时处理同一个房源预定，也能保证同一时间只有一个线程/服务能操作这个房源

        List<RLock> locks = new ArrayList<>();

        try {
            // 依次尝试获取每一天的锁
            for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                String lockKey = "booking:lock:listing:" + listingId + ":" + date;
                RLock lock = redissonClient.getLock(lockKey);
                // 尝试获取锁，最多等待10秒，锁持有时间最多30秒
                if (!lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                    throw new InvalidBookingException("Unable to acquire lock for date: " + date);
                }
                locks.add(lock);
            }

        /*
          lock.tryLock(10, 30, TimeUnit.SECONDS) 执行这个相当于：
          Redisson 底层会在 Redis 里执行类似 SET lockKey someUniqueValue NX PX 30000 的操作，
          其中 lockKey 是你定义的锁名称（比如 "booking:lock:listing:123"），
          someUniqueValue 是一个唯一标识用来区分不同客户端或线程，
          NX 表示只有在 key 不存在时才设置，PX 30000 表示锁的过期时间为 30 秒。
          这个操作相当于在 Redis 里“创建一个门锁”，拿到 key 就表示你获得了锁，其他线程或服务再尝试获取同样的锁会失败。
          锁释放时，Redisson 会检查 key 的值是否与自己持有的唯一标识一致，确认无误后才删除 key，从而释放锁。
         */

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

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidBookingException("Booking process was interrupted. Please try again.");
        } finally {
            // 确保在方法结束时释放所有锁
            for (RLock lock : locks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    // 当调用 lock.unlock() 释放锁时，Redisson 并不会直接删掉 Redis 里的 key，而是先检查这个 key 的值是不是自己之前设置的那个唯一标识。
                    // 如果是自己设置的值，说明确实是自己持有的锁，就安全地删除 key，释放锁。
                    // 如果不是自己设置的值，说明锁可能已经被其他线程或服务抢到或续租了，这时不会删除 key，避免误删别人持有的锁。
                }
            }
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
