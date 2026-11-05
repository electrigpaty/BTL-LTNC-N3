package com.auction.server.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private static final String URL = "jdbc:sqlite:auction.db";

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tái thiết lập kết nối DB: " + e.getMessage(), e);
        }
        return connection;
    }
}