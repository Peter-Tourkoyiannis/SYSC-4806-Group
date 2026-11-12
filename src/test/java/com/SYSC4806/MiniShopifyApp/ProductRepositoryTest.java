package com.SYSC4806.MiniShopifyApp;

import com.SYSC4806.MiniShopifyApp.model.Merchant;
import com.SYSC4806.MiniShopifyApp.model.Shop;
import com.SYSC4806.MiniShopifyApp.model.Product;

import com.SYSC4806.MiniShopifyApp.repository.MerchantRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductRepositoryTest {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void canSaveAndFindProduct() {

        // Create and save a merchant
        Merchant merchant = new Merchant("Test Merchant");
        merchant = merchantRepository.save(merchant);

        // Create and save a shop
        Shop shop = new Shop(
                "Test Shop",
                "A shop for testing",
                Set.of("electronics"),
                merchant
        );

        shop = shopRepository.save(shop);
        Long shopId = shop.getId();

        // Create a product
        Product product = new Product();
        product.setName("Laptop");
        product.setDescription("A laptop for testing");
        product.setPrice(999.99);
        product.setInventory(10);
        product.setShop(shop);

        // Save the product
        Product savedProduct = productRepository.save(product);

        Long productId = savedProduct.getId();

        entityManager.flush();
        entityManager.clear();

        // Retrieve the product
        Product foundProduct = productRepository
                .findById(productId)
                .orElseThrow();

        // Verify the results
        assertNotNull(productId);
        assertEquals("Laptop", foundProduct.getName());
        assertEquals("A laptop for testing",
                foundProduct.getDescription());
        assertEquals(999.99, foundProduct.getPrice(), 0.001);
        assertEquals(10, foundProduct.getInventory());
        assertEquals(shopId, foundProduct.getShop().getId());
    }
}
