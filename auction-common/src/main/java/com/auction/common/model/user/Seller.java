package com.auction.common.model.user;

public class Seller extends User {
    private static final long serialVersionUID = 1L;

    private String storeName;
    private double rating;

    public Seller() {
        super();
    }

    public Seller(String id, String username, String password, String email, String storeName) {
        super(id, username, password, email);
        this.storeName = storeName;
        this.rating = 5.0;
    }

    @Override
    public String getRoleName() {
        return "SELLER";
    }

    @Override
    public String printInfo() {
        return super.printInfo() + String.format(" | Cửa hàng: %s | Đánh giá: %.1f*", storeName, rating);
    }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}