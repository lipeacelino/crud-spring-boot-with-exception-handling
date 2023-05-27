package com.example.api.dto;

public record UpdateProductDto(

        String name,

        String description,

        Boolean available

) {
}