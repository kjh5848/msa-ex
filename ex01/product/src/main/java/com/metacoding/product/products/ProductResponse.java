package com.metacoding.product.products;

public class ProductResponse {
    public record  DTO(
        int id,
        String productName,
        int quantity,
        Long price
    ) {
        public DTO(Product product)  {
            this(product.getId(), product.getProductName(), product.getQuantity(), product.getPrice());
        }
    }
}
