package com.SYSC4806.MiniShopifyApp.controller;

import com.SYSC4806.MiniShopifyApp.model.Merchant;
import com.SYSC4806.MiniShopifyApp.model.Product;
import com.SYSC4806.MiniShopifyApp.model.Shop;
import com.SYSC4806.MiniShopifyApp.repository.MerchantRepository;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/merchant")
public class MerchantController {

    private final MerchantRepository merchantRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    public MerchantController(MerchantRepository merchantRepository, ShopRepository shopRepository, ProductRepository productRepository) {
        this.merchantRepository = merchantRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
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

    @PostMapping("/shops/{shopId}/products")
    public String addProduct(@PathVariable Long shopId,
                             @RequestParam String name,
                             @RequestParam String description,
                             @RequestParam double price,
                             @RequestParam int inventory,
                             @RequestParam("image") MultipartFile file) throws IOException {
        Shop shop = shopRepository.findById(shopId).orElseThrow();
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setInventory(inventory);
        product.setShop(shop);

        if (!file.isEmpty()) {
            product.setImageBytes(file.getBytes());
        }

        productRepository.save(product);
        return "redirect:/merchant/shops/" + shopId + "/dashboard";
    }

    @GetMapping("/products/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        if (product.getImageBytes() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(product.getImageBytes(), headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}