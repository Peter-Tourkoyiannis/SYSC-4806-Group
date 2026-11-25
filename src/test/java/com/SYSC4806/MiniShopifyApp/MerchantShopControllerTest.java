package com.SYSC4806.MiniShopifyApp;

import com.SYSC4806.MiniShopifyApp.controller.EditShopForm;
import com.SYSC4806.MiniShopifyApp.controller.MerchantForm;
import com.SYSC4806.MiniShopifyApp.controller.MerchantShopController;
import com.SYSC4806.MiniShopifyApp.controller.ShopForm;
import com.SYSC4806.MiniShopifyApp.model.Merchant;
import com.SYSC4806.MiniShopifyApp.model.Shop;
import com.SYSC4806.MiniShopifyApp.repository.MerchantRepository;
import com.SYSC4806.MiniShopifyApp.repository.ShopRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantShopControllerTest {

    @Test
    void createMerchantSavesMerchantAndRedirectsToShopCreationWithName() {
        MerchantRepository merchantRepository = mock(MerchantRepository.class);
        MerchantShopController controller = new MerchantShopController(merchantRepository, null);
        when(merchantRepository.save(any(Merchant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        MerchantForm form = new MerchantForm();
        form.setName(" New Merchant ");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(form, "merchantForm");
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String redirect = controller.createMerchant(form, bindingResult, redirectAttributes);

        assertEquals("redirect:/merchant/shops/new", redirect);
        verify(merchantRepository).save(argThat(merchant -> "New Merchant".equals(merchant.getName())));
        verify(redirectAttributes).addAttribute("merchantName", "New Merchant");
    }

    @Test
    void createMerchantRejectsDuplicateName() {
        MerchantRepository merchantRepository = mock(MerchantRepository.class);
        when(merchantRepository.findAllByNameIgnoreCase("Existing Merchant"))
                .thenReturn(List.of(new Merchant("Existing Merchant")));
        MerchantShopController controller = new MerchantShopController(merchantRepository, null);
        MerchantForm form = new MerchantForm();
        form.setName("Existing Merchant");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(form, "merchantForm");
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String view = controller.createMerchant(form, bindingResult, redirectAttributes);

        assertEquals("create-merchant", view);
        assertEquals(1, bindingResult.getFieldErrorCount("name"));
        verify(merchantRepository, never()).save(any(Merchant.class));
    }

    @Test
    void merchantHomeListsShopsForSelectedMerchantName() {
        MerchantRepository merchantRepository = mock(MerchantRepository.class);
        ShopRepository shopRepository = mock(ShopRepository.class);
        Merchant merchant = new Merchant("Garden Goods");
        merchant.setId(12L);
        List<Shop> shops = List.of(new Shop());
        when(merchantRepository.findAll(any(Sort.class))).thenReturn(List.of(merchant));
        when(merchantRepository.findAllByNameIgnoreCase("Garden Goods")).thenReturn(List.of(merchant));
        when(shopRepository.findByMerchant_NameIgnoreCase("Garden Goods")).thenReturn(shops);
        MerchantShopController controller = new MerchantShopController(merchantRepository, shopRepository);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.merchantHome("Garden Goods", model);

        assertEquals("merchant-home", view);
        assertEquals(Set.of("Garden Goods"), model.getAttribute("merchants"));
        assertEquals("Garden Goods", model.getAttribute("selectedMerchantName"));
        assertEquals(shops, model.getAttribute("shops"));
    }

    @Test
    void editShopUpdatesNameDescriptionAndTags() {
        ShopRepository shopRepository = mock(ShopRepository.class);
        Shop shop = new Shop();
        when(shopRepository.findById(7L)).thenReturn(Optional.of(shop));
        MerchantShopController controller = new MerchantShopController(null, shopRepository);
        EditShopForm form = new EditShopForm();
        form.setName("Garden Goods");
        form.setDescription("Tools for gardeners");
        form.setTagsString("garden, gifts, garden, ");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(form, "editShopForm");

        String redirect = controller.editShop(7L, form, bindingResult, new ExtendedModelMap());

        assertEquals("Garden Goods", shop.getName());
        assertEquals("Tools for gardeners", shop.getDescription());
        assertEquals(Set.of("garden", "gifts"), shop.getTags());
        assertEquals("redirect:/merchant/shops/7/dashboard", redirect);
        verify(shopRepository).save(shop);
    }

    @Test
    void shopFormRequiresMerchantNameShopNameAndDescription() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();
            Set<String> invalidShopFields = validator.validate(new ShopForm()).stream()
                    .map(violation -> violation.getPropertyPath().toString())
                    .collect(Collectors.toSet());
            Set<String> invalidEditFields = validator.validate(new EditShopForm()).stream()
                    .map(violation -> violation.getPropertyPath().toString())
                    .collect(Collectors.toSet());

            assertEquals(Set.of("merchantName", "shopName", "description"), invalidShopFields);
            assertEquals(Set.of("name", "description"), invalidEditFields);
        }
    }
}
