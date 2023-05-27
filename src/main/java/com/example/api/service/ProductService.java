package com.example.api.service;

import com.example.api.dto.*;
import com.example.api.entities.Product;
import com.example.api.entities.ProductVariation;
import com.example.api.enums.Category;
import com.example.api.exception.ProductNotFoundException;
import com.example.api.exception.ProductVariationNotFoundException;
import com.example.api.exception.ProductVariationUnavailableException;
import com.example.api.mapper.ProductMapper;
import com.example.api.repositories.ProductRepository;
import com.example.api.repositories.ProductVariationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariationRepository productVariationRepository;

    @Autowired
    private ProductMapper productMapper;

    // Método responsável por criar um produto
    public RecoveryProductDto createProduct(CreateProductDto createProductDto) {
        /*
        Converte a lista de ProductVariationDto em uma lista de ProductVariation,
        utilizando o ProductMapper para fazer o mapeamento de cada elemento da lista.
         */
        List<ProductVariation> productVariations =  createProductDto.productVariations().stream()
                .map(productVariationDto -> productMapper.mapCreateProductVariationDtoToProductVariation(productVariationDto))
                .toList();

        // Cria um produto através dos dados do DTO
        Product product = Product.builder()
                .name(createProductDto.name())
                .description(createProductDto.description())
                .category(Category.valueOf(createProductDto.category().toUpperCase()))
                .productVariations(productVariations)
                .available(createProductDto.available())
                .build();

        /*
        Se o produto estiver com o available = false, por padrão todas as variações do produto devem estar com available false também,
        porque não faria sentido o produto estar estar indisponível e as variações daquele produto estarem disponíveis
         */
        if (!product.getAvailable() && product.getProductVariations().stream().anyMatch(ProductVariation::getAvailable)) {
            throw new ProductVariationUnavailableException();
        }

        // Relaciona cada variação de produto com o produto
        productVariations.forEach(productVariation -> productVariation.setProduct(product));

        // Salva produto
        Product productSaved = productRepository.save(product);

        // Retornando e mapeando os produtos para o tipo RecoveryProductDto
        return productMapper.mapProductToRecoveryProductDto(productSaved);
    }

    // Método responsável por criar uma variação de produto
    public RecoveryProductDto createProductVariation(Long productId, CreateProductVariationDto createProductVariationDto) {

        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        // Converte o DTO de criação da variação de produto para uma entidade ProductVariation
        ProductVariation productVariation = productMapper.mapCreateProductVariationDtoToProductVariation(createProductVariationDto);

        // Associa a variação de produto ao produto e salva a variação no banco de dados
        productVariation.setProduct(product);
        ProductVariation productVariationSaved = productVariationRepository.save(productVariation);

        // Adiciona a variação de produto ao produto e salva o produto no banco de dados
        product.getProductVariations().add(productVariationSaved);
        productRepository.save(product);

        // Retornando e mapeando os produtos para o tipo RecoveryProductDto
        return productMapper.mapProductToRecoveryProductDto(productVariationSaved.getProduct());
    }

    // Método responsável por retornar todos os produtos
    public List<RecoveryProductDto> getProducts() {
        // Retorna todos os produtos salvos no banco
        List<Product> products = productRepository.findAll();

        // Retornando e mapeando os produtos para uma lista do tipo RecoveryProductDto
        return products.stream().map(product -> productMapper.mapProductToRecoveryProductDto(product)).toList();
    }

    // Método responsável por retornar o produto por id
    public RecoveryProductDto getProductById(Long productId) {
        // Producra por um produto salvo no banco
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        // Retornando e mapeando os produtos para o tipo RecoveryProductDto
        return productMapper.mapProductToRecoveryProductDto(product);
    }

    // Atualiza um produto (sem atualizar as variações dele)
    public RecoveryProductDto updateProductPart(Long productId, UpdateProductDto updateProductDto) {
        // Procura por um produto salvo no banco
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        /*
        Aqui temos uma sequência de if que serve como uma forma de programação defensiva: só altera o valor se foi passado algum valor no Json
         */
        if (updateProductDto.name() != null) {
            product.setName(updateProductDto.name());
        }
        if (updateProductDto.description() != null) {
            product.setDescription(updateProductDto.description());
        }
        if (updateProductDto.available() != null) {
            product.setAvailable(updateProductDto.available());

            /*
            Se o produto estiver com o available = false, por padrão todas as variações do produto devem estar com available = false também,
            porque não faria sentido o produto estar estar indisponível e as variações daquele produto estarem disponíveis
             */
            if (!product.getAvailable()) {
                product.getProductVariations().forEach(productVariation -> productVariation.setAvailable(false));
            }
        }

        // Retornando e mapeando os produtos para o tipo RecoveryProductDto
        return productMapper.mapProductToRecoveryProductDto(productRepository.save(product));
    }

    // Método responsável por atualizar uma variação de produto
    public RecoveryProductDto updateProductVariation(Long productId, Long productVariationId, UpdateProductVariationDto updateProductVariationDto) {
        // Verifica se o produto existe
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        /*
         Procura pela variação de produto (através do id) na lista de variações do produto
         que já está salvo no banco
         */
        ProductVariation productVariation = product.getProductVariations().stream()
                .filter(productVariationInProduct -> productVariationInProduct.getId().equals(productVariationId))
                .findFirst()
                .orElseThrow(ProductVariationNotFoundException::new);

        if (updateProductVariationDto.sizeName() != null) {
            productVariation.setSizeName(updateProductVariationDto.sizeName());
        }
        if (updateProductVariationDto.description() != null) {
            productVariation.setDescription(updateProductVariationDto.description());
        }
        if (updateProductVariationDto.available() != null) {
            /*
            Se o produto estiver com o available = false, por padrão a nova variação adicionada deve estar com available = false também,
            porque não faria sentido o produto estar estar indisponível e a variação daquele produto estar disponível
             */
            if (updateProductVariationDto.available() && !productVariation.getProduct().getAvailable()) {
                throw new ProductVariationUnavailableException();
            }
            productVariation.setAvailable(updateProductVariationDto.available());
        }
        if (updateProductVariationDto.price() != null) {
            productVariation.setPrice(updateProductVariationDto.price());
        }

        // Salva um produto no banco de dados
        Product productSaved = productRepository.save(product);

        // Retornando e mapeando os produtos para o tipo RecoveryProductDto
        return productMapper.mapProductToRecoveryProductDto(productSaved);
    }

    // Método responsável por deletar um produto pelo id
    public void deleteProductId(Long productId) {
        // Verifica se o produto existe
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException();
        }
        // Deleta um produto do banco de dados
        productRepository.deleteById(productId);
    }

    // Método responsável por deletar uma variação de produto pelo id
    public void deleteProductVariationById(Long productId, Long productVariationId) {
        // Verifica se a variação de produto existe no produto em questão
        ProductVariation productVariation = productVariationRepository
                .findByProductIdAndProductVariationId(productId, productVariationId)
                .orElseThrow(ProductVariationNotFoundException::new);

        // Deleta a variação de produto do banco de dados
        productVariationRepository.deleteById(productVariation.getId());
    }
}
