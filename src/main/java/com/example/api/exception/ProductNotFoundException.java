package com.example.api.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() {
        super("Produto não encontrado.");
    }

}
