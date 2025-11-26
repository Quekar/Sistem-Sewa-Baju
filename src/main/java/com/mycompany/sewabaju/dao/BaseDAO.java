package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.database.DatabaseConnection;
import com.mycompany.sewabaju.exceptions.DatabaseException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T> {
    
    protected Connection connection;
    public BaseDAO() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    protected abstract String getTableName();
    protected abstract String getPrimaryKeyColumn();

    public T findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getPrimaryKeyColumn() + " = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding " + getTableName() + " by ID: " + id, e);
        }
        
        return null;
    }
    
    public List<T> findAll() throws DatabaseException {
        String sql = "SELECT * FROM " + getTableName();
        List<T> results = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all " + getTableName(), e);
        }
        
        return results;
    }
    
    public boolean delete(int id) throws DatabaseException {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getPrimaryKeyColumn() + " = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting " + getTableName() + " with ID: " + id, e);
        }
    }
    
    public int count() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting " + getTableName(), e);
        }
        
        return 0;
    }
    
    public boolean exists(int id) throws DatabaseException {
        String sql = "SELECT 1 FROM " + getTableName() + " WHERE " + getPrimaryKeyColumn() + " = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking existence in " + getTableName(), e);
        }
    }
    
    protected List<T> executeQuery(String sql, Object... params) throws DatabaseException {
        List<T> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error executing query: " + sql, e);
        }
        
        return results;
    }
    
    protected int executeUpdate(String sql, Object... params) throws DatabaseException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                setParameter(stmt, i + 1, params[i]);
            }
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error executing update: " + sql, e);
        }
    }
    
    protected int executeInsertWithGeneratedKey(String sql, Object... params) throws DatabaseException {
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                setParameter(stmt, i + 1, params[i]);
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error executing insert: " + sql, e);
        }
        
        return -1;
    }
    
    private void setParameter(PreparedStatement stmt, int index, Object value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.NULL);
        } else if (value instanceof Enum) {
            stmt.setString(index, ((Enum<?>) value).name());
        } else if (value instanceof java.time.LocalDate) {
            stmt.setDate(index, Date.valueOf((java.time.LocalDate) value));
        } else if (value instanceof java.time.LocalDateTime) {
            stmt.setTimestamp(index, Timestamp.valueOf((java.time.LocalDateTime) value));
        } else {
            stmt.setObject(index, value);
        }
    }
}