package com.laioffer.staybooking.geography;

import com.laioffer.staybooking.model.response.AmapGeocodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "amapClient", url = "https://restapi.amap.com")
public interface AmapClient {
    @GetMapping("/v3/geocode/geo")
    AmapGeocodeResponse geocode(
            @RequestParam("address") String address,
            @RequestParam("key") String key
    );
}
