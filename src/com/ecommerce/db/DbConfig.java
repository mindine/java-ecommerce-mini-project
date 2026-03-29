package com.ecommerce.db;

//public final class DbConfig {
//    public static final String URL = System.getenv("DB_URL");
//    public static final String USER = System.getenv("DB_USER");
//    public static final String PASS = System.getenv("DB_PASS");
//    private DbConfig() {}
//
//}

public class DbConfig {
    public static final String URL = "jdbc:postgresql://localhost:5432/ecommerce_db";
    public static final String USER = "postgres";
    public static final String PASS = ""; // Postgres.app default is usually empty
}
