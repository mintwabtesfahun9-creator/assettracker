package com.i.a.dto;

import com.i.a.entity.Role;

public record UserResponse(
    Long id,
    String username,
    Role role
) {}
