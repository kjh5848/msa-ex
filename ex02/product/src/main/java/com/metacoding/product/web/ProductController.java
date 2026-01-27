package com.metacoding.product.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.metacoding.product.core.util.Resp;
import com.metacoding.product.usecase.*;
import com.metacoding.product.web.dto.ProductResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final GetProductUseCase getProductUseCase;
    private final GetAllProductsUseCase getAllProductsUseCase;
    private final DecreaseQuantityUseCase decreaseQuantityUseCase;
    private final IncreaseQuantityUseCase increaseQuantityUseCase;

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable("productId") int productId) {
        ProductResponse response = getProductUseCase.findById(productId);
        return Resp.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getProducts() {
        List<ProductResponse> responses = getAllProductsUseCase.findAll();
        return Resp.ok(responses);
    }

    @PostMapping("/{productId}/decrease")
    public ResponseEntity<?> decreaseQuantity(@PathVariable("productId") int productId, @RequestParam("quantity") int quantity) {
        decreaseQuantityUseCase.decreaseQuantity(productId, quantity);
        return Resp.ok(null);
    }

    @PostMapping("/{productId}/increase")
    public ResponseEntity<?> increaseQuantity(@PathVariable("productId") int productId, @RequestParam("quantity") int quantity) {
        increaseQuantityUseCase.increaseQuantity(productId, quantity);
        ProductResponse response = getProductUseCase.findById(productId);
        return Resp.ok(response);
    }
}


















