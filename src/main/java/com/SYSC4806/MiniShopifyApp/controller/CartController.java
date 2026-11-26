package com.SYSC4806.MiniShopifyApp.controller;

import com.SYSC4806.MiniShopifyApp.cart.Cart;
import com.SYSC4806.MiniShopifyApp.model.Product;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Set;
import java.util.HashSet;

@Controller
public class CartController {

    private final Cart cart;
    private final ProductRepository productRepository;

    public CartController(Cart cart,
                          ProductRepository productRepository) {
        this.cart = cart;
        this.productRepository = productRepository;
    }

    @GetMapping("/cart")
    public String viewCart(Model model) {

        Map<Long, Integer> items = cart.getItems();

        List<Product> products =
                productRepository.findAllById(items.keySet());

        // Collect the IDs of products that still exist
        Set<Long> existingIds = new HashSet<>();

        for (Product product : products) {
            existingIds.add(product.getId());
        }

        // Remove products that no longer exist
        for (Long productId : items.keySet()) {
            if (!existingIds.contains(productId)) {
                cart.remove(productId);

                model.addAttribute("warning",
                        "Some products are no longer available and have been removed.");
            }
        }

        // Retrieve the updated cart contents
        items = cart.getItems();

        double total = 0;

        for (Product product : products) {
            total += product.getPrice() * items.get(product.getId());
        }

        model.addAttribute("products", products);
        model.addAttribute("quantities", items);
        model.addAttribute("total", total);
        model.addAttribute("cartItemCount", cart.getItemCount());

        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity,
            RedirectAttributes redirectAttributes) {

        Optional<Product> result = productRepository.findById(productId);

        if (result.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Product not found");
            return "redirect:/cart";
        }

        Product product = result.get();

        if (quantity <= 0) {
            redirectAttributes.addFlashAttribute("error", "Invalid quantity");
            return "redirect:/cart";
        }

        int currentQuantity = cart.getItems().getOrDefault(productId, 0);

        if (quantity > product.getInventory() - currentQuantity) {
            redirectAttributes.addFlashAttribute("error", "Not enough stock");
            return "redirect:/cart";
        }

        cart.add(productId, quantity);

        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity,
            RedirectAttributes redirectAttributes) {

        Optional<Product> result = productRepository.findById(productId);

        if (result.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Product not found");
            return "redirect:/cart";
        }

        Product product = result.get();

        if (!cart.getItems().containsKey(productId)) {
            redirectAttributes.addFlashAttribute("error", "Product is not in the cart");
            return "redirect:/cart";
        }

        if (quantity <= 0) {
            redirectAttributes.addFlashAttribute("error", "Invalid quantity");
            return "redirect:/cart";
        }

        if (quantity > product.getInventory()) {
            redirectAttributes.addFlashAttribute("error", "Not enough stock");
            return "redirect:/cart";
        }

        cart.update(productId, quantity);

        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(
            @RequestParam("productId") Long productId) {

        cart.remove(productId);

        return "redirect:/cart";
    }
}