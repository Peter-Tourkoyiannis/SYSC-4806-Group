package com.SYSC4806.MiniShopifyApp.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.LocalDateTime;

@Entity
@Table(name= "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime createdAt = LocalDateTime.now();
    private double total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    List<OrderItem> orderItems = new ArrayList<>();

    public Order() {}

    public double getTotal() { 
        return total; 
    }

    public void setTotal(double total) { 
        this.total = total; 
    }

    public Long getId() { 
        return id; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}
