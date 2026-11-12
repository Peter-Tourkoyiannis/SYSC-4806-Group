package com.SYSC4806.MiniShopifyApp;

import com.SYSC4806.MiniShopifyApp.model.Merchant;
import com.SYSC4806.MiniShopifyApp.repository.MerchantRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MerchantRepositoryTest {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void canSaveAndFindMerchant() {

        Merchant merchant = new Merchant("Test Merchant");

        Merchant savedMerchant = merchantRepository.save(merchant);

        Long merchantId = savedMerchant.getId();

        entityManager.flush();
        entityManager.clear();

        Merchant foundMerchant = merchantRepository
                .findById(merchantId)
                .orElseThrow();

        assertNotNull(merchantId);
        assertEquals("Test Merchant", foundMerchant.getName());
    }
}