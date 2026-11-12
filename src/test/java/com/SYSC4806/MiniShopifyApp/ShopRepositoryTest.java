package com.SYSC4806.MiniShopifyApp;

import com.SYSC4806.MiniShopifyApp.model.Merchant;
import com.SYSC4806.MiniShopifyApp.model.Shop;
import com.SYSC4806.MiniShopifyApp.repository.MerchantRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class ShopRepositoryTest {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void canSaveAndFindShop() {

        // Create and save a merchant
        Merchant merchant = new Merchant("Test Merchant");
        merchant = merchantRepository.save(merchant);

        // Create a shop belonging to that merchant
        Shop shop = new Shop(
                "Test Shop",
                "A shop for testing",
                Set.of("electronics", "computers"),
                merchant
        );

        // Save the shop
        Shop savedShop = shopRepository.save(shop);

        Long shopId = savedShop.getId();

        entityManager.flush();
        entityManager.clear();

        // Retrieve the shop
        Shop foundShop = shopRepository
                .findById(shopId)
                .orElseThrow();

        // Verify the results
        assertNotNull(shopId);
        assertEquals("Test Shop", foundShop.getName());
        assertEquals("A shop for testing",
                foundShop.getDescription());
        assertEquals(Set.of("electronics", "computers"),
                foundShop.getTags());
        assertEquals(merchant.getId(),
                foundShop.getMerchant().getId());
    }
}
