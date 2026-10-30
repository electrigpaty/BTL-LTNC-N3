package com.auction.common.model.item;

public class ItemFactory {
    private ItemFactory() {}

    public static Item createItem(String type, String id, String name, String description,
                                  double startingPrice, String sellerId,
                                  String extraStringAttr, int extraIntAttr) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Loại sản phẩm không được để trống.");
        }
        switch (type.trim().toUpperCase()) {
            case "ELECTRONICS":
                return new Electronics(id, name, description, startingPrice, sellerId, extraStringAttr, extraIntAttr);
            case "ART":
                return new Art(id, name, description, startingPrice, sellerId, extraStringAttr, extraIntAttr);
            case "VEHICLE":
                return new Vehicle(id, name, description, startingPrice, sellerId, extraStringAttr, extraIntAttr);
            default:
                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ: " + type);
        }
    }
}