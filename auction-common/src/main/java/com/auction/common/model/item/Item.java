package com.auction.common.model.item;

import com.auction.common.model.Entity;

public abstract class Item extends Entity {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private double startingPrice;
    private String sellerId;
    private String imageUrl;

    protected Item() {
        super();
    }

    protected Item(String id, String name, String description, double startingPrice, String sellerId) {
        super(id);
        if (startingPrice <= 0) {
            throw new IllegalArgumentException("Giá khởi điểm phải lớn hơn 0.");
        }
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.sellerId = sellerId;
    }


    public abstract String getCategoryType();

    @Override
    public String printInfo() {
        return String.format("[%s] %s - Giá khởi điểm: %.2f", getCategoryType(), name, startingPrice);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) {
        if (startingPrice <= 0) {
            throw new IllegalArgumentException("Giá khởi điểm phải lớn hơn 0.");
        }
        this.startingPrice = startingPrice;
    }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}