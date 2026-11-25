package com.SYSC4806.MiniShopifyApp.controller;

import jakarta.validation.constraints.NotBlank;

public class MerchantForm {

    @NotBlank(message = "Merchant name is required.")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
