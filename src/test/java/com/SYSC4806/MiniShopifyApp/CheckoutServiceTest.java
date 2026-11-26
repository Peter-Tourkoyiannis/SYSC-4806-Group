
package com.SYSC4806.MiniShopifyApp;

import com.SYSC4806.MiniShopifyApp.model.Merchant;
import com.SYSC4806.MiniShopifyApp.model.Shop;
import com.SYSC4806.MiniShopifyApp.model.Product;
import com.SYSC4806.MiniShopifyApp.model.Order;

import com.SYSC4806.MiniShopifyApp.repository.MerchantRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;
import com.SYSC4806.MiniShopifyApp.repository.OrderRepository;
import com.SYSC4806.MiniShopifyApp.service.CheckoutService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CheckoutServiceTest {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CheckoutService checkoutService;

    private Long productId;

    @BeforeEach
    void setUp() {

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

        // Create and save a product
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("A product for testing");
        product.setPrice(20.00);
        product.setInventory(10);
        product.setShop(shop);

        Product savedProduct = productRepository.save(product);
        productId = savedProduct.getId();
    }

    @Test
    void successOrder() {

        long ordersBefore = orderRepository.count();

        // Purchase 3 units of the product
        Map<Long, Integer> cart = Map.of(productId, 3);

        Order order = checkoutService.checkoutCart(cart);

        // Verify the order was created and saved
        assertNotNull(order.getId());
        assertEquals(60.00, order.getTotal(), 0.001);
        assertTrue(orderRepository.findById(order.getId()).isPresent());
        assertEquals(ordersBefore + 1, orderRepository.count());

        // Verify inventory decreased from 10 to 7
        Product updatedProduct = productRepository.findById(productId)
                .orElseThrow();

        assertEquals(7, updatedProduct.getInventory());
    }

    @Test
    void insufficientStock() {

        long ordersBefore = orderRepository.count();

        // Try to purchase 15 units when only 10 exist
        Map<Long, Integer> cart = Map.of(productId, 15);

        try {
            checkoutService.checkoutCart(cart);
            fail("Checkout should reject insufficient stock");
        } catch (IllegalArgumentException e) {
            // Expected: checkout rejects the purchase
            }

        // Verify inventory remains unchanged
        Product unchangedProduct = productRepository.findById(productId).orElseThrow();

        assertEquals(10, unchangedProduct.getInventory());

        // Verify no order was saved
        assertEquals(ordersBefore, orderRepository.count());
    }

    @Test
    void emptyCart() {

        long ordersBefore = orderRepository.count();

        Map<Long, Integer> cart = Map.of();

        try {
            checkoutService.checkoutCart(cart);
            fail("Checkout should reject an empty cart");
        } catch (IllegalArgumentException e) {
            // Expected: checkout rejects the empty cart
            }

        assertEquals(ordersBefore, orderRepository.count());
    }
}
