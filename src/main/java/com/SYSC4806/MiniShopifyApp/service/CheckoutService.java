package com.SYSC4806.MiniShopifyApp.service;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;
import com.SYSC4806.MiniShopifyApp.model.Product;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class CheckoutService {

    private ProductRepository productRepository;

    public CheckoutService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void checkoutCart(Map<Long, Integer> cart) {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty!");
        }
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            if (productId == null || productRepository.findById(productId).isEmpty()) {
                throw new IllegalArgumentException("Product must have a valid ID!");
            }

            if (quantity == null || quantity < 1) {
                throw new IllegalArgumentException("Requested quantity must be at least 1!");
            }

            Product product = productRepository.findById(productId).orElseThrow();
            if (product.getInventory() < quantity) {
                throw new IllegalArgumentException("Insufficient stock for requested quantity!");
            }
        }
    }
}