package com.SYSC4806.MiniShopifyApp;

import com.SYSC4806.MiniShopifyApp.controller.EditShopForm;
import com.SYSC4806.MiniShopifyApp.controller.ShopForm;
import com.SYSC4806.MiniShopifyApp.model.Merchant;
import com.SYSC4806.MiniShopifyApp.model.Shop;
import com.SYSC4806.MiniShopifyApp.repository.MerchantRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MerchantShopFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void getCreateMerchantFormRenders() throws Exception {
        mockMvc.perform(get("/merchant/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-merchant"))
                .andExpect(model().attributeExists("merchantForm"));
    }

    @Test
    void validMerchantIsSavedAndSelectedOnCreateShopForm() throws Exception {
        MvcResult result = mockMvc.perform(post("/merchant/merchants")
                        .param("name", "New Merchant"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertEquals("/merchant/shops/new?merchantName=New+Merchant",
                result.getResponse().getRedirectedUrl());
        assertEquals(1, merchantRepository.findAllByNameIgnoreCase("New Merchant").size());

        mockMvc.perform(get("/merchant/shops/new").param("merchantName", "New Merchant"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-shop"))
                .andExpect(model().attribute("merchants", Set.of("New Merchant")))
                .andExpect(mvcResult -> {
                    ShopForm form = (ShopForm) mvcResult.getModelAndView().getModel().get("shopForm");
                    assertEquals("New Merchant", form.getMerchantName());
                });
    }

    @Test
    void blankMerchantNameRendersValidationErrorWithoutSaving() throws Exception {
        long merchantCount = merchantRepository.count();

        mockMvc.perform(post("/merchant/merchants").param("name", "  "))
                .andExpect(status().isOk())
                .andExpect(view().name("create-merchant"))
                .andExpect(model().attributeHasFieldErrors("merchantForm", "name"));

        assertEquals(merchantCount, merchantRepository.count());
    }

    @Test
    void getCreateShopFormIncludesMerchantDropdownOptions() throws Exception {
        merchantRepository.save(new Merchant("Existing Merchant"));

        mockMvc.perform(get("/merchant/shops/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-shop"))
                .andExpect(model().attribute("merchants", Set.of("Existing Merchant")))
                .andExpect(content().string(containsString("<select id=\"merchantName\"")))
                .andExpect(content().string(containsString("<option value=\"Existing Merchant\">")));
    }

    @Test
    void validShopIsSavedForSelectedMerchantAndRedirectsToDashboard() throws Exception {
        Merchant merchant = merchantRepository.save(new Merchant("Shop Owner"));

        MvcResult result = mockMvc.perform(post("/merchant/shops")
                        .param("merchantName", "Shop Owner")
                        .param("shopName", "New Shop")
                        .param("description", "A test shop")
                        .param("tagsString", "garden, gifts"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectUrl = result.getResponse().getRedirectedUrl();
        assertTrue(redirectUrl != null && redirectUrl.matches("/merchant/shops/\\d+/dashboard"));

        List<Shop> shops = shopRepository.findByMerchant_NameIgnoreCase("Shop Owner");
        assertEquals(1, shops.size());
        assertEquals("New Shop", shops.get(0).getName());
        assertEquals("A test shop", shops.get(0).getDescription());
        assertEquals(Set.of("garden", "gifts"), shops.get(0).getTags());
        assertEquals(merchant.getId(), shops.get(0).getMerchant().getId());

        mockMvc.perform(get(redirectUrl))
                .andExpect(status().isOk())
                .andExpect(view().name("shop-dashboard"));
    }

    @Test
    void blankShopNameRendersValidationErrorWithoutSaving() throws Exception {
        merchantRepository.save(new Merchant("Shop Owner"));

        mockMvc.perform(post("/merchant/shops")
                        .param("merchantName", "Shop Owner")
                        .param("shopName", " ")
                        .param("description", "A test shop")
                        .param("tagsString", "garden"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-shop"))
                .andExpect(model().attributeHasFieldErrors("shopForm", "shopName"))
                .andExpect(model().attribute("merchants", Set.of("Shop Owner")));

        assertTrue(shopRepository.findByMerchant_NameIgnoreCase("Shop Owner").isEmpty());
    }

    @Test
    void getEditShopFormPrefillsNameDescriptionAndTags() throws Exception {
        Shop shop = createShop("Existing Shop", "Existing description", Set.of("home", "decor"));

        mockMvc.perform(get("/merchant/shops/{shopId}/edit", shop.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-shop"))
                .andExpect(model().attribute("shopId", shop.getId()))
                .andExpect(result -> {
                    EditShopForm form = (EditShopForm) result.getModelAndView().getModel().get("editShopForm");
                    assertEquals("Existing Shop", form.getName());
                    assertEquals("Existing description", form.getDescription());
                    assertEquals(Set.of("home", "decor"),
                            Set.copyOf(Arrays.asList(form.getTagsString().split(", "))));
                });
    }

    @Test
    void validShopEditPersistsNameDescriptionAndTags() throws Exception {
        Shop shop = createShop("Existing Shop", "Existing description", Set.of("home"));
        Long shopId = shop.getId();

        mockMvc.perform(post("/merchant/shops/{shopId}/edit", shopId)
                        .param("name", "Updated Shop")
                        .param("description", "Updated description")
                        .param("tagsString", "garden, tools"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/merchant/shops/" + shopId + "/dashboard"));

        entityManager.flush();
        entityManager.clear();
        Shop updated = shopRepository.findById(shopId).orElseThrow();
        assertEquals("Updated Shop", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(Set.of("garden", "tools"), updated.getTags());
    }

    @Test
    void blankShopNameRejectsEditAndDoesNotChangeShop() throws Exception {
        Shop shop = createShop("Existing Shop", "Existing description", Set.of("home"));
        Long shopId = shop.getId();

        mockMvc.perform(post("/merchant/shops/{shopId}/edit", shopId)
                        .param("name", " ")
                        .param("description", "Changed description")
                        .param("tagsString", "garden"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-shop"))
                .andExpect(model().attributeHasFieldErrors("editShopForm", "name"));

        Shop unchanged = shopRepository.findById(shopId).orElseThrow();
        assertEquals("Existing Shop", unchanged.getName());
        assertEquals("Existing description", unchanged.getDescription());
        assertEquals(Set.of("home"), unchanged.getTags());
    }

    @Test
    void merchantLookupOnlyDisplaysSelectedMerchantsShops() throws Exception {
        createShop("Selected Shop", "Selected description", Set.of("selected"), "Selected Merchant");
        createShop("Other Shop", "Other description", Set.of("other"), "Other Merchant");

        mockMvc.perform(get("/merchant/home").param("merchantName", "Selected Merchant"))
                .andExpect(status().isOk())
                .andExpect(view().name("merchant-home"))
                .andExpect(model().attribute("shops",
                        org.hamcrest.Matchers.hasSize(1)))
                .andExpect(content().string(containsString("Selected Shop")))
                .andExpect(content().string(not(containsString("Other Shop"))));
    }

    @Test
    void dashboardViewAsCustomerLinkOpensThatShopsStorefront() throws Exception {
        Shop shop = createShop("Customer View Shop", "Customer-facing description", Set.of("featured"));

        mockMvc.perform(get("/merchant/shops/{shopId}/dashboard", shop.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("shop-dashboard"))
                .andExpect(content().string(
                        containsString("href=\"/shops/" + shop.getId() + "\"")));

        mockMvc.perform(get("/shops/{shopId}", shop.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("storefront"))
                .andExpect(model().attribute("shop", shop));
    }

    private Shop createShop(String name, String description, Set<String> tags) {
        return createShop(name, description, tags, "Shop Owner");
    }

    private Shop createShop(String name, String description, Set<String> tags, String merchantName) {
        Merchant merchant = merchantRepository.save(new Merchant(merchantName));
        return shopRepository.save(new Shop(name, description, tags, merchant));
    }
}
