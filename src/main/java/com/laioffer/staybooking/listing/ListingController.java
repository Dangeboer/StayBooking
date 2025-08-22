package com.laioffer.staybooking.listing;

import com.laioffer.staybooking.booking.BookingService;
import com.laioffer.staybooking.model.BookingDto;
import com.laioffer.staybooking.model.ListingDto;
import com.laioffer.staybooking.model.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/listings")
public class ListingController {

    private final BookingService bookingService;
    private final ListingService listingService;

    public ListingController(BookingService bookingService, ListingService listingService) {
        this.bookingService = bookingService;
        this.listingService = listingService;
    }

    // 通过房东id找到房源信息
    @GetMapping
    public List<ListingDto> getListings(@AuthenticationPrincipal UserEntity user) {
        return listingService.getListings(user.getId());
    }

    // 创建新的房源信息
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createListing(
            @AuthenticationPrincipal UserEntity user,
            @RequestParam("name") String name,
            @RequestParam("address") String address,
            @RequestParam("description") String description,
            @RequestParam("guest_number") int guestNumber,
            @RequestParam("images") List<MultipartFile> images // 图片和pdf都不能放在Json里，所以不能用Body。这个叫做multi-part?
    ) {
        listingService.createListing(user.getId(), name, address, description, guestNumber, images);
    }

    // 删除某个房源
    @DeleteMapping("/{listingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteListing(@AuthenticationPrincipal UserEntity user, @PathVariable Long listingId) {
        listingService.deleteListing(user.getId(), listingId);
    }

    // 搜索可用房源
    @GetMapping("/search")
    public List<ListingDto> search(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam("checkin_date") LocalDate checkIn,
            @RequestParam("checkout_date") LocalDate checkOut,
            @RequestParam("guest_number") int guestNumber,
            @RequestParam(name = "distance", required = false) Integer distance
    ) {
        if (distance == null) {
            distance = 500000;
        }
        return listingService.search(lat, lon, distance, checkIn, checkOut, guestNumber);
    }

    // 通过房源id找到预定信息
    @GetMapping("/{listingId}/bookings")
    public List<BookingDto> getListingBookings(@AuthenticationPrincipal UserEntity user, @PathVariable Long listingId) {
        return bookingService.findBookingsByListingId(user.getId(), listingId);
    }
}
