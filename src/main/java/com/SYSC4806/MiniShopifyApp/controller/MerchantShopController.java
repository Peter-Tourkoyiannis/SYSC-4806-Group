package com.SYSC4806.MiniShopifyApp.controller;

import com.SYSC4806.MiniShopifyApp.model.Merchant;
import com.SYSC4806.MiniShopifyApp.model.Shop;
import com.SYSC4806.MiniShopifyApp.repository.MerchantRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/merchant")
public class MerchantShopController {

    private final MerchantRepository merchantRepository;
    private final ShopRepository shopRepository;

    public MerchantShopController(MerchantRepository merchantRepository, ShopRepository shopRepository) {
        this.merchantRepository = merchantRepository;
        this.shopRepository = shopRepository;
    }

    @GetMapping("/home")
    public String merchantHome(@RequestParam(required = false) String merchantName, Model model) {
        Set<String> merchantNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        merchantRepository.findAll(Sort.by("name")).forEach(merchant -> merchantNames.add(merchant.getName()));
        model.addAttribute("merchants", merchantNames);
        if (merchantName != null && !merchantName.isBlank()) {
            List<Merchant> matchingMerchants = merchantRepository.findAllByNameIgnoreCase(merchantName.trim());
            if (matchingMerchants.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Merchant not found");
            }
            model.addAttribute("selectedMerchantName", matchingMerchants.get(0).getName());
            model.addAttribute("shops", shopRepository.findByMerchant_NameIgnoreCase(merchantName.trim()));
        }
        return "merchant-home";
    }

    @GetMapping("/new")
    public String showCreateMerchantForm(Model model) {
        model.addAttribute("merchantForm", new MerchantForm());
        return "create-merchant";
    }

    @PostMapping("/merchants")
    public String createMerchant(@Valid @ModelAttribute("merchantForm") MerchantForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "create-merchant";
        }

        String merchantName = form.getName().trim();
        if (!merchantRepository.findAllByNameIgnoreCase(merchantName).isEmpty()) {
            bindingResult.rejectValue("name", "duplicate", "A merchant with this name already exists.");
            return "create-merchant";
        }

        Merchant merchant = merchantRepository.save(new Merchant(merchantName));
        redirectAttributes.addAttribute("merchantName", merchant.getName());
        return "redirect:/merchant/shops/new";
    }

    @GetMapping("/shops/new")
    public String showCreateShopForm(@RequestParam(required = false) String merchantName, Model model) {
        ShopForm form = new ShopForm();
        form.setMerchantName(merchantName);
        model.addAttribute("shopForm", form);
        model.addAttribute("merchants", getMerchantNames());
        return "create-shop";
    }

    @PostMapping("/shops")
    public String createShop(@Valid @ModelAttribute("shopForm") ShopForm form,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("merchants", getMerchantNames());
            return "create-shop";
        }

        String merchantName = form.getMerchantName().trim();
        List<Merchant> matchingMerchants = merchantRepository.findAllByNameIgnoreCase(merchantName);
        Merchant merchant = matchingMerchants.isEmpty()
                ? merchantRepository.save(new Merchant(merchantName))
                : matchingMerchants.get(0);
        Shop shop = new Shop(form.getShopName().trim(), form.getDescription().trim(),
                parseTags(form.getTagsString()), merchant);
        shopRepository.save(shop);
        return "redirect:/merchant/shops/" + shop.getId() + "/dashboard";
    }

    @GetMapping("/shops/{shopId}/dashboard")
    public String shopDashboard(@PathVariable Long shopId, Model model) {
        Shop shop = shopRepository.findById(shopId).orElseThrow();
        model.addAttribute("shop", shop);
        return "shop-dashboard";
    }

    @GetMapping("/shops/{shopId}/edit")
    public String showEditShopForm(@PathVariable Long shopId, Model model) {
        Shop shop = shopRepository.findById(shopId).orElseThrow();
        EditShopForm form = new EditShopForm();
        form.setName(shop.getName());
        form.setDescription(shop.getDescription());
        form.setTagsString(String.join(", ", shop.getTags()));
        model.addAttribute("editShopForm", form);
        model.addAttribute("shopId", shopId);
        return "edit-shop";
    }

    @PostMapping("/shops/{shopId}/edit")
    public String editShop(@PathVariable Long shopId,
                           @Valid @ModelAttribute("editShopForm") EditShopForm form,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("shopId", shopId);
            return "edit-shop";
        }

        Shop shop = shopRepository.findById(shopId).orElseThrow();
        shop.setName(form.getName().trim());
        shop.setDescription(form.getDescription().trim());
        shop.setTags(parseTags(form.getTagsString()));
        shopRepository.save(shop);
        return "redirect:/merchant/shops/" + shopId + "/dashboard";
    }

    private Set<String> parseTags(String tagsString) {
        if (tagsString == null || tagsString.isBlank()) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet()));
    }

    private Set<String> getMerchantNames() {
        Set<String> merchantNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        merchantRepository.findAll(Sort.by("name")).forEach(merchant -> merchantNames.add(merchant.getName()));
        return merchantNames;
    }
}
