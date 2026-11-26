package com.SYSC4806.MiniShopifyApp.cart;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashMap;
import java.util.Map;

@Component
@SessionScope
public class Cart {

    private final Map<Long, Integer> items = new HashMap<>();

    public Map<Long, Integer> getItems() {
        return Map.copyOf(items);
    }

    public void add(Long productId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        items.merge(productId, qty, Integer::sum);
    }

    public void clear() {
        items.clear();
    }

    public void update(Long productId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (!items.containsKey(productId)) {
            throw new IllegalArgumentException("Product is not in the cart");
        }

        items.put(productId, qty);
    }

    public void remove(Long productId) {
        items.remove(productId);
    }

    public int getItemCount() {
        int count = 0;

        for (int quantity : items.values()) {
            count += quantity;
        }

        return count;
    }
}