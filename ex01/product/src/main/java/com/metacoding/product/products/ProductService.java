package com.metacoding.product.products;

import com.metacoding.product.core.handler.ex.Exception404;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse.DTO findById(int productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception404("상품이 없습니다."));
        return new ProductResponse.DTO(product);
    }

    public List<ProductResponse.DTO> findAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse.DTO::new)
                .toList();
    }

    @Transactional
    public ProductResponse.DTO decreaseQuantity(int productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception404("상품이 없습니다."));
        product.decreaseQuantity(quantity);
        return new ProductResponse.DTO(product);
    }

    @Transactional
    public ProductResponse.DTO increaseQuantity(int productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception404("상품이 없습니다."));
        product.increaseQuantity(quantity);
        return new ProductResponse.DTO(product);
    }
}
