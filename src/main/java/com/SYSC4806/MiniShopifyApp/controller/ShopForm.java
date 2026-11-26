package com.SYSC4806.MiniShopifyApp.controller;

import jakarta.validation.constraints.NotBlank;

public class ShopForm {

    @NotBlank(message = "Merchant name is required.")
    private String merchantName;

    @NotBlank(message = "Shop name is required.")
    private String shopName;

    @NotBlank(message = "Shop description is required.")
    private String description;

    private String tagsString = "";

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTagsString() {
        return tagsString;
    }

    public void setTagsString(String tagsString) {
        this.tagsString = tagsString;
    }
}
