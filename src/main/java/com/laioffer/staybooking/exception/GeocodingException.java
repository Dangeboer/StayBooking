package com.laioffer.staybooking.exception;


public class GeocodingException extends RuntimeException {
    public GeocodingException() {
        super("Failed to look up address");
    }
}
