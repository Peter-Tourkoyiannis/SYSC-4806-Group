package com.SYSC4806.MiniShopifyApp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index"; // Landing page with Merchant / Customer buttons
    }
}