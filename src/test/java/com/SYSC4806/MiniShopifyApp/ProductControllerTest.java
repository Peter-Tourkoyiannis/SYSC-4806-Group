package com.SYSC4806.MiniShopifyApp;

import com.SYSC4806.MiniShopifyApp.model.Merchant;
import com.SYSC4806.MiniShopifyApp.model.Product;
import com.SYSC4806.MiniShopifyApp.model.Shop;
import com.SYSC4806.MiniShopifyApp.repository.MerchantRepository;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private MockMvc mockMvc;
    private Shop shop;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Merchant merchant = merchantRepository.save(new Merchant("Test Merchant"));
        shop = shopRepository.save(new Shop("Test Shop", "Description", new HashSet<>(), merchant));

    }

    private MockMultipartFile pngImage() {

        byte[] bytes = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jR1sAAAAASUVORK5CYII=");
        return new MockMultipartFile("image", "product.png", "image/png", bytes);

    }

    @Test
    void testPngImageUploadAndRetrieval() throws Exception {
        MockMultipartFile image = pngImage();
        mockMvc.perform(multipart("/merchant/shops/{shopId}/products", shop.getId()).file(image).param("name", "Test Product").param("description", "Description").param("price", "10.00").param("inventory", "5")).andExpect(status().is3xxRedirection());

        entityManager.flush();
        entityManager.clear();

        Product product = productRepository.findAll().stream().filter(p -> shop.getId().equals(p.getShop().getId())).findFirst().orElseThrow();
        mockMvc.perform(get("/merchant/products/{id}/image", product.getId())).andExpect(status().isOk()).andExpect(content().contentType("image/png")).andExpect(content().bytes(image.getBytes()));

    }

    @Test
    void testSvgImageRejected() throws Exception {

        long count = productRepository.count();

        MockMultipartFile image = new MockMultipartFile("image", "product.svg", "image/svg+xml", "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/merchant/shops/{shopId}/products", shop.getId()).file(image).param("name", "Test Product").param("description", "Description").param("price", "10.00").param("inventory", "5")).andExpect(status().isUnsupportedMediaType());

        assertEquals(count, productRepository.count());
    }

    @Test
    void testStockUpdate() throws Exception {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Description");
        product.setPrice(10.00);
        product.setInventory(5);
        product.setShop(shop);
        product = productRepository.saveAndFlush(product);

        Long id = product.getId();
        String version = product.getVersion().toString();

        entityManager.clear();
        mockMvc.perform(post("/merchant/products/{id}/stock", id).param("inventory", "20").param("version", version)).andExpect(status().is3xxRedirection());
        entityManager.flush();
        entityManager.clear();

        assertEquals(20, productRepository.findById(id).orElseThrow().getInventory());
    }

    @Test
    void testNegativePriceRejected() throws Exception {
        long count = productRepository.count();

        mockMvc.perform(multipart("/merchant/shops/{shopId}/products", shop.getId()).file(pngImage()).param("name", "Test Product").param("description", "Description").param("price", "-10.00").param("inventory", "5")).andExpect(status().isBadRequest());

        assertEquals(count, productRepository.count());
    }
}