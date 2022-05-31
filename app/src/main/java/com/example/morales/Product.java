package com.example.morales;

import com.google.firebase.database.Exclude;

import java.util.Locale;

public class Product {
    private String name;
    private String nameLower;
    private String imageURL;
    private String key;
    private String description;
    private String price;
    private String quantity;
    private int position;

    public Product() {
        //empty constructor needed
    }
    public Product (int position){
        this.position = position;
    }

    public Product(String name, String nameLower, String imageUrl ,String Des, String price, String quantity) {
        if (name.trim().equals("")) {
            name = "No name";
            nameLower = "no name";
        }
        this.name = name;
        this.nameLower = nameLower.toLowerCase();
        this.imageURL = imageUrl;
        this.description = Des;
        this.price = price;
        this.quantity = quantity;
    }

    public String getNameLower() {
        return nameLower;
    }

    public void setNameLower(String nameLower) {
        this.nameLower = nameLower.toLowerCase();
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getImageUrl() {
        return imageURL;
    }
    public void setImageUrl(String imageUrl) {
        this.imageURL = imageUrl;
    }
    @Exclude
    public String getKey() {
        return key;
    }
    @Exclude
    public void setKey(String key) {
        this.key = key;
    }
}
