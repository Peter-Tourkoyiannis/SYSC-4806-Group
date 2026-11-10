package com.SYSC4806.MiniShopifyApp.repository;

import com.SYSC4806.MiniShopifyApp.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
}