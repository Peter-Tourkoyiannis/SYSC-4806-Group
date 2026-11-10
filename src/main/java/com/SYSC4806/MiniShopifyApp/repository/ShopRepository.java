package com.SYSC4806.MiniShopifyApp.repository;

import com.SYSC4806.MiniShopifyApp.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByNameContainingIgnoreCase(String name);
    List<Shop> findByTagsContaining(String tag);
}