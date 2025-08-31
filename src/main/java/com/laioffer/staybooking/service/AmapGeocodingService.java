package com.laioffer.staybooking.service;

import com.laioffer.staybooking.map.AmapClient;
import com.laioffer.staybooking.model.AmapGeocodeResponse;
import com.laioffer.staybooking.model.dto.GeoPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AmapGeocodingService {

    private final AmapClient amapClient;
    private final String amapKey;

    public AmapGeocodingService(
            AmapClient amapClient,
            @Value("${staybooking.geocoding.amap-key}") String amapKey
    ) {
        this.amapClient = amapClient;
        this.amapKey = amapKey;
    }

    public GeoPoint getGeoPoint(String address) {
        AmapGeocodeResponse response = amapClient.geocode(address, amapKey);
        if (!"1".equals(response.status()) || response.geocodes().isEmpty()) {
            throw new RuntimeException("地址解析失败: " + address);
        }

        String[] parts = response.geocodes().get(0).location().split(",");
        double lng = Double.parseDouble(parts[0]);
        double lat = Double.parseDouble(parts[1]);
        return new GeoPoint(lat, lng);
    }
}
