package com.example.api.dto;

import java.math.BigDecimal;

public record RecoveryProductVariationDto(

        Long id,

        String sizeName,

        String description,

        BigDecimal price,

        Boolean available

) {
}
