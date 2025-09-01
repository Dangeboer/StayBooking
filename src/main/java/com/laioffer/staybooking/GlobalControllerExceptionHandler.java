package com.laioffer.staybooking;

import com.laioffer.staybooking.exception.DeleteBookingNotAllowedException;
import com.laioffer.staybooking.exception.InvalidBookingException;
import com.laioffer.staybooking.exception.ListingBookingsNotAllowedException;
import com.laioffer.staybooking.exception.DeleteListingNotAllowedException;
import com.laioffer.staybooking.exception.InvalidListingSearchException;
import com.laioffer.staybooking.model.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public final ResponseEntity<ErrorResponse> handleException(EntityNotFoundException e) {
        return new ResponseEntity<>(new ErrorResponse(
                "Resource not found",
                "resource_not_found"),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ListingBookingsNotAllowedException.class)
    public final ResponseEntity<ErrorResponse> handleException(ListingBookingsNotAllowedException e) {
        return new ResponseEntity<>(new ErrorResponse(
                "Cannot get listing bookings",
                "listing_bookings_not_allowed"),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DeleteBookingNotAllowedException.class)
    public final ResponseEntity<ErrorResponse> handleException(DeleteBookingNotAllowedException e) {
        return new ResponseEntity<>(new ErrorResponse(
                "Cannot delete booking",
                "delete_booking_not_allowed"),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidBookingException.class)
    public final ResponseEntity<ErrorResponse> handleException(InvalidBookingException e) {
        return new ResponseEntity<>(new ErrorResponse(
                e.getMessage(),
                "invalid_booking_request"),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DeleteListingNotAllowedException.class)
    public final ResponseEntity<ErrorResponse> handleException(DeleteListingNotAllowedException e) {
        return new ResponseEntity<>(new ErrorResponse(
                e.getMessage(),
                "delete_listing_not_allowed"),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidListingSearchException.class)
    public final ResponseEntity<ErrorResponse> handleException(InvalidListingSearchException ex) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(),
                "invalid_search_request"),
                HttpStatus.BAD_REQUEST);
    }
}

// 如果定义好了exception但没有加在这里，能够成功报错但不会报具体的错