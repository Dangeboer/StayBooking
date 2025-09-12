package com.laioffer.staybooking.geography;

import com.laioffer.staybooking.model.dto.GeoPoint;

public interface GeocodingStrategy {
    GeoPoint getGeoPoint(String address);
}
