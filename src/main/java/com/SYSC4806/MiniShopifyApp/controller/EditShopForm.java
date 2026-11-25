package com.SYSC4806.MiniShopifyApp.controller;

import jakarta.validation.constraints.NotBlank;

public class EditShopForm {

    @NotBlank(message = "Shop name is required.")
    private String name;

    @NotBlank(message = "Shop description is required.")
    private String description;

    private String tagsString = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
