package com.SYSC4806.MiniShopifyApp;

import com.SYSC4806.MiniShopifyApp.model.Product;
import com.SYSC4806.MiniShopifyApp.model.Shop;
import com.SYSC4806.MiniShopifyApp.repository.ProductRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ProductDetailTest {
    @Autowired MockMvc mvc;
    @Autowired ShopRepository shops;
    @Autowired ProductRepository products;

    private Product product(Shop shop, String name) {
        Product p = new Product();
        p.setName(name); p.setDescription("d"); p.setPrice(9.99); p.setInventory(3); p.setShop(shop);
        return products.save(p);
    }

    @Test
    void showsProductInItsShop() throws Exception {
        Shop shop = shops.save(new Shop("S", "d", new HashSet<>(), null));
        Product p = product(shop, "Laptop");

        mvc.perform(get("/shops/{s}/products/{p}", shop.getId(), p.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Laptop")))
                .andExpect(content().string(containsString("$9.99")));
    }

    @Test
    void productFromAnotherShopIs404() throws Exception {
        Shop a = shops.save(new Shop("A", "d", new HashSet<>(), null));
        Shop b = shops.save(new Shop("B", "d", new HashSet<>(), null));
        Product p = product(b, "Laptop");

        mvc.perform(get("/shops/{s}/products/{p}", a.getId(), p.getId()))
                .andExpect(status().isNotFound());
    }
}
