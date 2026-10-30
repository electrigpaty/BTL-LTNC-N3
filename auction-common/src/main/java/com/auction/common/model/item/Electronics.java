package com.auction.common.model.item;

public class Electronics extends Item {
    private static final long serialVersionUID = 1L;

    private String brand;
    private int warrantyMonths;

    public Electronics() { super(); }

    public Electronics(String id, String name, String description, double startingPrice,
                       String sellerId, String brand, int warrantyMonths) {
        super(id, name, description, startingPrice, sellerId);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String getCategoryType() { return "ELECTRONICS"; }

    @Override
    public String printInfo() {
        return super.printInfo() + String.format(" | Hãng: %s | Bảo hành: %d tháng", brand, warrantyMonths);
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }
}