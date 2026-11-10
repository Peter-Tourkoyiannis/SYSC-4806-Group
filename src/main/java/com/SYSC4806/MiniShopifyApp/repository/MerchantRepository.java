package com.SYSC4806.MiniShopifyApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SYSC4806.MiniShopifyApp.model.Merchant;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
}