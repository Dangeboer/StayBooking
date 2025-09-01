package com.laioffer.staybooking.model.reqeust;

public record HotRequest(
        String name,
        Long districtCode,
        String address) {
}
