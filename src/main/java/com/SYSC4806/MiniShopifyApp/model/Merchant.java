package com.SYSC4806.MiniShopifyApp.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "merchants")
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Shop> shops = new ArrayList<>();

    public Merchant() {}
    public Merchant(String name) { this.name = name; }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Shop> getShops() { return shops; }
    public void setShops(List<Shop> shops) { this.shops = shops; }
}