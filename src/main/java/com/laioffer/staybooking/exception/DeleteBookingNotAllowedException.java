package com.laioffer.staybooking.exception;

public class DeleteBookingNotAllowedException extends RuntimeException {

    public DeleteBookingNotAllowedException(long guestId, long bookingId) {
        // super是继承
        super("Guest " + guestId + " not allow to delete the booking " + bookingId);
    }
}
