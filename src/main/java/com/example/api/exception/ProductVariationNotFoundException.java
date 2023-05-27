package com.example.api.exception;

public class ProductVariationNotFoundException extends RuntimeException {
    public ProductVariationNotFoundException() {
        super("Variação de produto não encontrada.");
    }

}