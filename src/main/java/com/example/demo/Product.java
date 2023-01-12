package com.example.demo;

import lombok.Data;

@Data
public class Product {

    private String id;
    private String name;
    private Double price;
    private String currency;
    private Double discountRate;
    private String imageUrl;
    private String categoryName;
    private String marketName;
    private String link;
    private Double tax;
    private Double shippingFee;
    private int clickCount;
    private String locale;
    private Double naverPrice;
    private String productDescription; // HTML
}