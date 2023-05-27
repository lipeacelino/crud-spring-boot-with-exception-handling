package com.example.api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name="product_variations")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductVariation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sizeName;

    private String description;

    private Boolean available;

    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

}