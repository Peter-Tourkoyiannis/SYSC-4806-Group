package com.SYSC4806.MiniShopifyApp.controller;

import com.SYSC4806.MiniShopifyApp.model.Product;
import com.SYSC4806.MiniShopifyApp.model.Shop;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.server.ResponseStatusException;
import java.util.Set;

import org.springframework.ui.Model;
import org.springframework.dao.OptimisticLockingFailureException;
import java.util.Objects;

import java.io.IOException;

@Controller
@RequestMapping("/merchant")
public class ProductController {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    public ProductController(ProductRepository productRepository, ShopRepository shopRepository) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
    }

    @PostMapping("/shops/{shopId}/products")
    public String addProduct(@PathVariable Long shopId, @RequestParam String name, @RequestParam String description, @RequestParam double price, @RequestParam int inventory, @RequestParam("image") MultipartFile file) throws IOException {

        validateProduct(name, description, price, inventory);

        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found"));

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setInventory(inventory);
        product.setShop(shop);

        updateProductImage(product, file);

        productRepository.save(product);

        return redirectToDashboard(shopId);
    }

    @GetMapping("/products/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {

        Product product = findProduct(id);

        if (product.getImageBytes() == null || product.getImageBytes().length == 0) {

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String contentType = product.getImageContentType();

        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set("X-Content-Type-Options", "nosniff");

        return new ResponseEntity<>(product.getImageBytes(), headers, HttpStatus.OK);
    }

    @GetMapping("/products/{id}/edit")
    public String showEditProductForm(@PathVariable Long id, Model model) {

        Product product = findProduct(id);
        model.addAttribute("product", product);
        return "edit-product";
    }

    @PostMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, @RequestParam String name, @RequestParam String description, @RequestParam double price, @RequestParam int inventory, @RequestParam Long version, @RequestParam(value = "image", required = false) MultipartFile file) throws IOException {

        validateProduct(name, description, price, inventory);
        Product product = findProduct(id);
        validateVersion(product, version);
        Long shopId = product.getShop().getId();

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setInventory(inventory);

        updateProductImage(product, file);
        saveProduct(product);

        return redirectToDashboard(shopId);
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, @RequestParam Long version) {

        Product product = findProduct(id);
        validateVersion(product, version);
        Long shopId = product.getShop().getId();

        try {
            productRepository.delete(product);
            productRepository.flush();
        } catch (OptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product has been modified. Please refresh and try again.");
        }

        return redirectToDashboard(shopId);
    }

    @PostMapping("/products/{id}/stock")
    public String updateStock(@PathVariable Long id, @RequestParam int inventory, @RequestParam Long version) {

        if (inventory < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory cannot be negative.");
        }

        Product product = findProduct(id);
        validateVersion(product, version);
        Long shopId = product.getShop().getId();

        product.setInventory(inventory);
        saveProduct(product);

        return redirectToDashboard(shopId);
    }

    // Helpers
    private void validateProduct(String name, String description, double price, int inventory) {

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required.");
        }

        if (description == null || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product description is required.");
        }

        if (!Double.isFinite(price) || price < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product price.");
        }

        if (inventory < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory cannot be negative.");
        }
    }

    private void updateProductImage(Product product, MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            return;
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {

            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported image format");
        }

        product.setImageBytes(file.getBytes());
        product.setImageContentType(contentType);
    }

    private String redirectToDashboard(Long shopId) {
        return "redirect:/merchant/shops/" + shopId + "/dashboard";
    }

    private void saveProduct(Product product) {

        try {
            productRepository.saveAndFlush(product);
        } catch (OptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product has been modified. Please refresh and try again.");
        }
    }

    private void validateVersion(Product product, Long version) {

        if (!Objects.equals(product.getVersion(), version)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product has been modified. Please refresh and try again.");
        }
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

}