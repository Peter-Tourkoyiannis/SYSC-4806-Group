package com.SYSC4806.MiniShopifyApp.model;

import jakarta.persistence.*;

@Entity
@Table(name= "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int quantity;

    @ManyToOne 
    private Product product;
    private double price;

    @ManyToOne
    private Order order;

    public OrderItem() {}

    public OrderItem(int quantity, double price, Product product, Order order) {
        this.quantity = quantity;
        this.price = price;
        this.product = product;
        this.order = order;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
