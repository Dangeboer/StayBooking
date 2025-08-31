package com.laioffer.staybooking.model;

import java.util.List;

public record AmapGeocodeResponse(
        String status,
        List<Geocode> geocodes
) {
    public record Geocode(
            String formatted_address,
            String location
    ) {}
}