package com.laioffer.staybooking.model.reqeust;

import com.laioffer.staybooking.model.UserRole;

public record RegisterRequest(
        String username,
        String password,
        UserRole role
) {
}