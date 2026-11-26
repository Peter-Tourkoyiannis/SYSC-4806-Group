package com.SYSC4806.MiniShopifyApp.service;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;
import com.SYSC4806.MiniShopifyApp.repository.OrderRepository;
import com.SYSC4806.MiniShopifyApp.model.Product;
import com.SYSC4806.MiniShopifyApp.model.Order;
import com.SYSC4806.MiniShopifyApp.model.OrderItem;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class CheckoutService {

    private ProductRepository productRepository;
    private OrderRepository orderRepository;

    public CheckoutService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }
    
    @Transactional
    public Order checkoutCart(Map<Long, Integer> cart) {
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

        Order order = new Order();
        double orderTotal = 0;

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            Product product = productRepository.findById(productId).orElseThrow();
            double price = product.getPrice();
            OrderItem orderItem = new OrderItem(quantity, price, product, order);
            order.addOrderItem(orderItem);
            orderTotal += price * quantity;
            int stock = product.getInventory();
            stock -= quantity;
            product.setInventory(stock);
        }

        order.setTotal(orderTotal);
        return orderRepository.save(order);
    }
}