package com.dario.clinapp.dao.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private static final String DATABASE_URL = "jdbc:sqlite:clinapp.db";
    private Connection connection;

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL);
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database", e);
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
                connection = DriverManager.getConnection(DATABASE_URL);
                connection.setAutoCommit(true);
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting database connection", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}