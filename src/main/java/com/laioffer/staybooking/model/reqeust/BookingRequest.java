package com.laioffer.staybooking.model.reqeust;

import java.time.LocalDate;

public record BookingRequest(
        long listingId,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
}
