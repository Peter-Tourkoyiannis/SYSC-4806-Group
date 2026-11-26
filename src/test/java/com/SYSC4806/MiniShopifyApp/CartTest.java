package com.SYSC4806.MiniShopifyApp;

import com.SYSC4806.MiniShopifyApp.cart.Cart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CartTest {

    @Test
    void testAddProduct() {
        Cart cart = new Cart();

        cart.add(1L, 2);

        assertEquals(2, cart.getItems().get(1L));
    }

    @Test
    void testMergeQuantities() {
        Cart cart = new Cart();

        cart.add(1L, 2);
        cart.add(1L, 3);

        assertEquals(5, cart.getItems().get(1L));
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void testUpdateProduct() {
        Cart cart = new Cart();

        cart.add(1L, 2);
        cart.update(1L, 4);

        assertEquals(4, cart.getItems().get(1L));
    }

    @Test
    void testRemoveProduct() {
        Cart cart = new Cart();

        cart.add(1L, 2);
        cart.remove(1L);

        assertFalse(cart.getItems().containsKey(1L));
    }

    @Test
    void testRemoveNonexistentProduct() {
        Cart cart = new Cart();

        cart.add(1L, 2);
        cart.remove(99L);

        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(1L));
    }

    @Test
    void testClearCart() {
        Cart cart = new Cart();

        cart.add(1L, 2);
        cart.add(2L, 3);

        cart.clear();

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void testInvalidAddQuantity() {
        Cart cart = new Cart();

        assertThrows(IllegalArgumentException.class,
                () -> cart.add(1L, -1));

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void testInvalidUpdateQuantity() {
        Cart cart = new Cart();

        cart.add(1L, 2);

        assertThrows(IllegalArgumentException.class,
                () -> cart.update(1L, 0));

        assertEquals(2, cart.getItems().get(1L));
    }

    @Test
    void testCartSnapshot() {
        Cart cart = new Cart();

        cart.add(1L, 2);

        var snapshot = cart.getItems();

        cart.remove(1L);

        assertEquals(2, snapshot.get(1L));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void testSnapshotCannotBeModified() {
        Cart cart = new Cart();

        cart.add(1L, 2);

        assertThrows(UnsupportedOperationException.class,
                () -> cart.getItems().put(1L, 10));

        assertEquals(2, cart.getItems().get(1L));
    }
}