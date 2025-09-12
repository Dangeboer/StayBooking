package com.laioffer.staybooking.geography;

import com.laioffer.staybooking.model.response.AmapGeocodeResponse;
import com.laioffer.staybooking.model.dto.GeoPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service("amapGeocoding")
@ConditionalOnProperty(prefix = "staybooking.geocoding", name = "provider", havingValue = "amap")
public class AmapGeocodingService implements GeocodingStrategy {

    private final AmapClient amapClient;
    private final String amapKey;

    public AmapGeocodingService(
            AmapClient amapClient,
            @Value("${staybooking.geocoding.amap-key}") String amapKey
    ) {
        this.amapClient = amapClient;
        this.amapKey = amapKey;
    }

    @Override
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
