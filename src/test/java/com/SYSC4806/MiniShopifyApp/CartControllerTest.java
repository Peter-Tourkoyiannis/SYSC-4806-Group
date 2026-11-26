package com.SYSC4806.MiniShopifyApp;

import com.SYSC4806.MiniShopifyApp.cart.Cart;
import com.SYSC4806.MiniShopifyApp.controller.CartController;
import com.SYSC4806.MiniShopifyApp.model.Product;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CartControllerTest {

    private MockMvc mockMvc;
    private Cart cart;
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        cart = new Cart();

        productRepository = mock(ProductRepository.class);

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(900);
        product.setInventory(5);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        mockMvc = MockMvcBuilders.standaloneSetup(
                new CartController(cart, productRepository)
        ).build();
    }

    @Test
    void testAddToCart() throws Exception {

        mockMvc.perform(post("/cart/add")
                        .param("productId", "1")
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        assertEquals(2, cart.getItems().get(1L));
    }

    @Test
    void testAddSameProductTwice() throws Exception {
        cart.add(1L, 2);

        mockMvc.perform(post("/cart/add")
                        .param("productId", "1")
                        .param("quantity", "3"))
                .andExpect(status().is3xxRedirection());

        assertEquals(5, cart.getItems().get(1L));
    }

    @Test
    void testAddExceedsStock() throws Exception {
        cart.add(1L, 3);

        mockMvc.perform(post("/cart/add")
                        .param("productId", "1")
                        .param("quantity", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Not enough stock"));

        assertEquals(3, cart.getItems().get(1L));
    }

    @Test
    void testAddInvalidQuantity() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .param("productId", "1")
                        .param("quantity", "-2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Invalid quantity"));

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void testAddNonexistentProduct() throws Exception {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/cart/add")
                        .param("productId", "99")
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Product not found"));

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void testUpdateCart() throws Exception {
        cart.add(1L, 2);

        mockMvc.perform(post("/cart/update")
                        .param("productId", "1")
                        .param("quantity", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        assertEquals(4, cart.getItems().get(1L));
    }

    @Test
    void testUpdateExceedsStock() throws Exception {
        cart.add(1L, 2);

        mockMvc.perform(post("/cart/update")
                        .param("productId", "1")
                        .param("quantity", "6"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Not enough stock"));

        assertEquals(2, cart.getItems().get(1L));
    }

    @Test
    void testRemoveFromCart() throws Exception {
        cart.add(1L, 2);

        mockMvc.perform(post("/cart/remove")
                        .param("productId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        assertTrue(cart.getItems().isEmpty());
    }
}