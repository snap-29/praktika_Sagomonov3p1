package com.example.crudapp.dao;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseProductDAO extends ProductDAO {

    @Override
    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
}