package com.laioffer.staybooking.exception;

public class ListingBookingsNotAllowedException extends RuntimeException {

    public ListingBookingsNotAllowedException(long hostId, long listingId) {
        super("Host " + hostId + " not allowed to get bookings of listing " + listingId);
    }
}