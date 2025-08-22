package com.laioffer.staybooking.model.dto;

import com.laioffer.staybooking.model.entity.UserEntity;
import com.laioffer.staybooking.model.UserRole;

public record UserDto(
        Long id,
        String username,
        UserRole role
) {
    public UserDto(UserEntity entity) {
        this(
                entity.getId(),
                entity.getUsername(),
                entity.getRole()
        );
    }
}