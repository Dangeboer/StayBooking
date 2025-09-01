package com.laioffer.staybooking.controller;

import com.laioffer.staybooking.service.BookingService;
import com.laioffer.staybooking.model.dto.BookingDto;
import com.laioffer.staybooking.model.reqeust.BookingRequest;
import com.laioffer.staybooking.model.entity.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // 通过住户id查询预定信息
    @GetMapping
    public List<BookingDto> getGuestBookings(@AuthenticationPrincipal UserEntity user) {
        return bookingService.findBookingsByGuestId(user.getId());
    }

    // 添加新的预定信息
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createBooking(@AuthenticationPrincipal UserEntity user, @RequestBody BookingRequest body) {
        bookingService.createBooking(user.getId(), body.listingId(), body.checkInDate(), body.checkOutDate());
    }

    // 通过预定id删除某个预定信息
    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@AuthenticationPrincipal UserEntity user, @PathVariable long bookingId) {
        bookingService.deleteBooking(user.getId(), bookingId);
    }
    // @PathVariable：作用是从 URL 路径中提取 bookingId，并将其作为 long 类型的参数传递给 deleteBooking 方法。
}
