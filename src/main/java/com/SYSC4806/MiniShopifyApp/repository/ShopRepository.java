package com.SYSC4806.MiniShopifyApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SYSC4806.MiniShopifyApp.model.Shop;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {


    //This query helps get a partial or full match on the requested tag
    @Query("""
        select distinct s from Shop s left join s.tags t where
                lower(s.name) like lower(concat('%', :q, '%')) or
                        lower(t) = lower(:q) order by s.name""")
    List<Shop> findByQ(String q);

}