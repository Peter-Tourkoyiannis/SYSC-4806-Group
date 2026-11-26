package com.SYSC4806.MiniShopifyApp.controller;

import com.SYSC4806.MiniShopifyApp.cart.Cart;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CartModelAdvice {

    private final Cart cart;

    public CartModelAdvice(Cart cart) {
        this.cart = cart;
    }

    @ModelAttribute("cartItemCount")
    public int cartItemCount() {
        return cart.getItemCount();
    }
}