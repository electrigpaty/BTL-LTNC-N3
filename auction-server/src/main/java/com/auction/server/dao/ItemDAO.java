package com.auction.server.dao;

import com.auction.server.config.DatabaseConnection;
import com.auction.common.model.item.Item;
import com.auction.common.model.item.ItemFactory;
import com.auction.common.model.item.Electronics;
import com.auction.common.model.item.Art;
import com.auction.common.model.item.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Item item = mapResultSetToItem(rs);
                if (item != null) {
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách sản phẩm: " + e.getMessage(), e);
        }
        return items;
    }

    public boolean save(Item item) {
        String sql = "INSERT INTO items(id, name, description, starting_price, seller_id, category_type, extra_string, extra_int) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            pstmt.setDouble(4, item.getStartingPrice());
            pstmt.setString(5, item.getSellerId());
            pstmt.setString(6, item.getCategoryType());

            String category = item.getCategoryType();
            if ("ELECTRONICS".equals(category)) {
                Electronics e = (Electronics) item;
                pstmt.setString(7, e.getBrand());
                pstmt.setInt(8, e.getWarrantyMonths());
            } else if ("ART".equals(category)) {
                Art a = (Art) item;
                pstmt.setString(7, a.getArtist());
                pstmt.setInt(8, a.getYearCreated());
            } else if ("VEHICLE".equals(category)) {
                Vehicle v = (Vehicle) item;
                pstmt.setString(7, v.getBrand());
                pstmt.setInt(8, v.getMileage());
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu sản phẩm vào database: " + e.getMessage(), e);
        }
    }

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        String type = rs.getString("category_type");
        String id = rs.getString("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double startingPrice = rs.getDouble("starting_price");
        String sellerId = rs.getString("seller_id");
        String extraString = rs.getString("extra_string");
        int extraInt = rs.getInt("extra_int");

        try {
            return ItemFactory.createItem(type, id, name, description, startingPrice, sellerId, extraString, extraInt);
        } catch (Exception e) {
            return null;
        }
    }
}