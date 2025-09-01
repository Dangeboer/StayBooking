package com.laioffer.staybooking.model.response;

public record ErrorResponse(
        String message,
        String error
) {
}