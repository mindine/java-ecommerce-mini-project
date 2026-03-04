package com.ecommerce.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASS);
    }
    private Db() {}
}
