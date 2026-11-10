package com.auction.server.dao;

import com.auction.server.config.DatabaseConnection;
import com.auction.common.model.user.User;
import com.auction.common.model.user.Bidder;
import com.auction.common.model.user.Seller;
import com.auction.common.model.user.Admin;

import java.sql.*;

public class UserDAO {

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn người dùng theo username: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean save(User user) {
        String sql = "INSERT INTO users(id, username, password, email, role, extra_attr1, extra_attr2) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getRoleName());

            if (user instanceof Bidder) {
                Bidder bidder = (Bidder) user;
                pstmt.setDouble(6, bidder.getWalletBalance());
                pstmt.setNull(7, Types.DOUBLE);
            } else if (user instanceof Seller) {
                Seller seller = (Seller) user;
                pstmt.setString(6, seller.getStoreName());
                pstmt.setDouble(7, seller.getRating());
            } else if (user instanceof Admin) {
                Admin admin = (Admin) user;
                pstmt.setString(6, admin.getDepartment());
                pstmt.setNull(7, Types.DOUBLE);
            } else {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.DOUBLE);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu người dùng vào database: " + e.getMessage(), e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String email = rs.getString("email");
        String role = rs.getString("role");

        if (role == null) return null;

        switch (role.trim().toUpperCase()) {
            case "BIDDER":
                double walletBalance = rs.getDouble("extra_attr1");
                return new Bidder(id, username, password, email, walletBalance);
            case "SELLER":
                String storeName = rs.getString("extra_attr1");
                Seller seller = new Seller(id, username, password, email, storeName);
                seller.setRating(rs.getDouble("extra_attr2"));
                return seller;
            case "ADMIN":
                String department = rs.getString("extra_attr1");
                return new Admin(id, username, password, email, department);
            default:
                return null;
        }
    }
}