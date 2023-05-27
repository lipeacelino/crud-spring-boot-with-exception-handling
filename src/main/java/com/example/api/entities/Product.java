package com.example.api.entities;

import com.example.api.enums.Category;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="products")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariation> productVariations;

    private Boolean available;

}