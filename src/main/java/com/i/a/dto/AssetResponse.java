package com.i.a.dto;

import com.i.a.entity.AssetStatus;
import java.time.LocalDateTime;

public record AssetResponse(
    Long id,
    String name,
    String serialNumber,
    AssetStatus status,
    Long categoryId,
    String categoryName,
    Long assignedToId,
    String assignedToUsername,
    LocalDateTime createdAt
) {}
