package com.example.crudapp.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:crud_app.db";
    private static Connection connection = null;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS products (" +
                "id TEXT PRIMARY KEY, " +
                "name VARCHAR(50) NOT NULL, " +
                "description TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String createUpdateTrigger = "CREATE TRIGGER IF NOT EXISTS update_product_timestamp " +
                "AFTER UPDATE ON products " +
                "BEGIN " +
                "UPDATE products SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id; " +
                "END;";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            stmt.execute(createUpdateTrigger);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}