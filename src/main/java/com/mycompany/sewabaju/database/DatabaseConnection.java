package com.mycompany.sewabaju.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private String url;
    private String username;
    private String password;
    
    private DatabaseConnection() {
        try {
            loadConfiguration();
            connect();
        } catch (IOException | SQLException e) {
            System.err.println("Error initializing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadConfiguration() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("com/mycompany/sewabaju/config/database.properties")) {
            
            if (input == null) {
                try (FileInputStream fileInput = new FileInputStream(
                        "src/main/resources/com/mycompany/sewabaju/config/database.properties")) {
                    props.load(fileInput);
                }
            } else {
                props.load(input);
            }
            
            this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/sewabaju_db");
            this.username = props.getProperty("db.username", "root");
            this.password = props.getProperty("db.password", "");
            String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            Class.forName(driver);
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            throw new IOException("Database driver not found", e);
        }
    }
    
    private void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection established successfully!");
        }
    }
    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }
    
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    public void reconnect() throws SQLException {
        closeConnection();
        connect();
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getUsername() {
        return username;
    }
}