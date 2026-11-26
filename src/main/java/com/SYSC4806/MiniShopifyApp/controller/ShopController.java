package com.SYSC4806.MiniShopifyApp.controller;

import com.SYSC4806.MiniShopifyApp.model.Product;
import com.SYSC4806.MiniShopifyApp.model.Shop;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class ShopController {

    private ShopRepository shopRepository;
    private ProductRepository productRepository;

    public ShopController(ShopRepository shopRepository, ProductRepository productRepository) {
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/shops")
    public String listShops(@RequestParam(required = false) String q, Model model) {
        List<Shop> shops;
        if (q == null)
            shops = shopRepository.findAll(Sort.by("name"));
        else
            shops = shopRepository.findByQ(q.trim());

        model.addAttribute("shops", shops);
        model.addAttribute("q", q == null ? "" : q.trim());
        return "shops";
    }

    @GetMapping("/shops/{id}")
    public String storefront(@PathVariable Long id, Model model) {
        Shop shop = shopRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Shop not found"));

        model.addAttribute("shop", shop);
        return "storefront";
    }

    @GetMapping("/shops/{shopId}/products/{productId}")
    public String productDetails(@PathVariable Long shopId, @PathVariable Long productId, Model model) {

        Product product = productRepository.findById(productId).
                filter(p -> p.getShop().getId().equals(shopId)).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        model.addAttribute("product", product);
        model.addAttribute("shop", product.getShop());
        return "product-details";
    }
}
