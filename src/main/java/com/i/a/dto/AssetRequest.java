package com.i.a.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssetRequest(
    @NotBlank(message = "Asset name is required")
    String name,

    @NotBlank(message = "Serial number is required")
    String serialNumber,

    @NotNull(message = "Category ID is required")
    Long categoryId
) {}
