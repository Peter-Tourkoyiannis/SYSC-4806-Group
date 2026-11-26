package com.SYSC4806.MiniShopifyApp;

import com.SYSC4806.MiniShopifyApp.model.Product;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class CartIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductRepository productRepository;

    private MockMvc mockMvc;
    private Product product;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        product = new Product();
        product.setName("Laptop");
        product.setPrice(900);
        product.setInventory(5);

        product = productRepository.saveAndFlush(product);
    }

    @Test
    void testSessionIsolation() throws Exception {

        MockHttpSession session1 = new MockHttpSession();
        MockHttpSession session2 = new MockHttpSession();

        mockMvc.perform(post("/cart/add")
                        .session(session1)
                        .param("productId", product.getId().toString())
                        .param("quantity", "2"))
                .andExpect(redirectedUrl("/cart"));

        mockMvc.perform(post("/cart/add")
                        .session(session2)
                        .param("productId", product.getId().toString())
                        .param("quantity", "3"))
                .andExpect(redirectedUrl("/cart"));

        mockMvc.perform(get("/cart").session(session1))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("quantities",
                        Map.of(product.getId(), 2)))
                .andExpect(model().attribute("total", 1800.0));

        mockMvc.perform(get("/cart").session(session2))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("quantities",
                        Map.of(product.getId(), 3)))
                .andExpect(model().attribute("total", 2700.0));
    }
}