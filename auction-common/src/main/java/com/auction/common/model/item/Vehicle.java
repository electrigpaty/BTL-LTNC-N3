package com.auction.common.model.item;

public class Vehicle extends Item {
    private static final long serialVersionUID = 1L;

    private String brand;
    private int mileage;

    public Vehicle() { super(); }

    public Vehicle(String id, String name, String description, double startingPrice,
                   String sellerId, String brand, int mileage) {
        super(id, name, description, startingPrice, sellerId);
        this.brand = brand;
        this.mileage = mileage;
    }

    @Override
    public String getCategoryType() { return "VEHICLE"; }

    @Override
    public String printInfo() {
        return super.printInfo() + String.format(" | Hãng xe: %s | ODO: %,d km", brand, mileage);
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }
}