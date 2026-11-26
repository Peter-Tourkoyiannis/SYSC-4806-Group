package com.SYSC4806.MiniShopifyApp.controller;

import com.SYSC4806.MiniShopifyApp.model.Merchant;
import com.SYSC4806.MiniShopifyApp.model.Shop;
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
@RequestMapping("/merchant")
public class MerchantController {

    private final MerchantRepository merchantRepository;
    private final ShopRepository shopRepository;

    public MerchantController(MerchantRepository merchantRepository, ShopRepository shopRepository) {
        this.merchantRepository = merchantRepository;
        this.shopRepository = shopRepository;
    }

    @GetMapping("/shops/new")
    public String showCreateShopForm(Model model) {
        return "create-shop";
    }

    @PostMapping("/shops")
    public String createShop(@RequestParam String merchantName,
                             @RequestParam String shopName,
                             @RequestParam String description,
                             @RequestParam String tagsString) {
        Merchant merchant = merchantRepository.save(new Merchant(merchantName));
        Set<String> tags = new HashSet<>(Arrays.stream(tagsString.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet()));

        Shop shop = new Shop(shopName, description, tags, merchant);
        shopRepository.save(shop);
        return "redirect:/merchant/shops/" + shop.getId() + "/dashboard";
    }

    @GetMapping("/shops/{shopId}/dashboard")
    public String shopDashboard(@PathVariable Long shopId, Model model) {
        Shop shop = shopRepository.findById(shopId).orElseThrow();
        model.addAttribute("shop", shop);
        return "shop-dashboard";
    }
}