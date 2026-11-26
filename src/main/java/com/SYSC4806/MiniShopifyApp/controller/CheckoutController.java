package com.SYSC4806.MiniShopifyApp.controller;

import com.SYSC4806.MiniShopifyApp.service.CheckoutService;
import com.SYSC4806.MiniShopifyApp.repository.OrderRepository;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;
import com.SYSC4806.MiniShopifyApp.model.Order;
import com.SYSC4806.MiniShopifyApp.model.Product;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import com.SYSC4806.MiniShopifyApp.cart.Cart;

@Controller
public class CheckoutController {
    
    private OrderRepository orderRepository;
    private CheckoutService checkoutService;
    private ProductRepository productRepository;
    private Cart cart;

    public CheckoutController(OrderRepository orderRepository, CheckoutService checkoutService, Cart cart, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.checkoutService = checkoutService;
        this.productRepository = productRepository;
        this.cart = cart;
    }

    @GetMapping("/orders/{id}")
    public String showOrder(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id).orElseThrow();
        model.addAttribute("order", order);
        return "order-confirmation";
    }

    @GetMapping("/checkout")
    public String showCheckout(Model model) {
        model.addAttribute("cartItems", cart.getItems());
        model.addAttribute("products", productRepository.findAllById(cart.getItems().keySet()));
        double total = 0;

        for (Product product : productRepository.findAllById(cart.getItems().keySet())) {
            total += product.getPrice() * cart.getItems().get(product.getId());
        }

        model.addAttribute("total", total);
        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(Model model) {
        try {
            Order order = checkoutService.checkoutCart(cart.getItems());
            cart.clear();
            return "redirect:/orders/" + order.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cartItems", cart.getItems());
            model.addAttribute("products", productRepository.findAllById(cart.getItems().keySet()));
            double total = 0;

            for (Product product : productRepository.findAllById(cart.getItems().keySet())) {
                total += product.getPrice() * cart.getItems().get(product.getId());
            }

            model.addAttribute("total", total);
            return "checkout";
        }
    }
}
