package com.SYSC4806.MiniShopifyApp.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.SYSC4806.MiniShopifyApp.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {}