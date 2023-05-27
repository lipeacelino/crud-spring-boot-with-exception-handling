package com.example.api.dto;

import java.math.BigDecimal;

public record UpdateProductVariationDto(

        String sizeName,

        String description,

        BigDecimal price,

        Boolean available

) {
}
