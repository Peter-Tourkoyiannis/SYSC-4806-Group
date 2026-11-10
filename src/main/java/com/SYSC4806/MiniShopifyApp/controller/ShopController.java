package com.SYSC4806.MiniShopifyApp.controller;

import com.SYSC4806.MiniShopifyApp.Merchant;
import com.SYSC4806.MiniShopifyApp.Shop;
import com.SYSC4806.MiniShopifyApp.repository.MerchantRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/shops")
public class ShopController {

    private final ShopRepository shopRepository;
    private final MerchantRepository merchantRepository;

    public ShopController(ShopRepository shopRepository, MerchantRepository merchantRepository) {
        this.shopRepository = shopRepository;
        this.merchantRepository = merchantRepository;
    }

    @GetMapping("/new")
    public String showCreateShopForm(Model model) {
        model.addAttribute("shop", new Shop());
        model.addAttribute("merchants", merchantRepository.findAll());
        return "create-shop";
    }

    // Handle form submission
    @PostMapping
    public String createShop(@RequestParam String name,
                             @RequestParam String description,
                             @RequestParam String tagsString,
                             @RequestParam Long merchantId) {
        
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseGet(() -> merchantRepository.save(new Merchant("Default Merchant", "test@merchant.com")));

        Set<String> tags = new HashSet<>(Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet()));

        Shop shop = new Shop(name, description, tags, merchant);
        shopRepository.save(shop);

        return "redirect:/shops";
    }

    // Simple shop list view (links up nicely with Person C's search features)
    @GetMapping
    public String listShops(@RequestParam(required = false) String keyword, Model model) {
        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("shops", shopRepository.findByNameContainingIgnoreCase(keyword));
        } else {
            model.addAttribute("shops", shopRepository.findAll());
        }
        return "shops";
    }
}