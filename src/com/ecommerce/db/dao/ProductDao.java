package com.ecommerce.db.dao;

import com.ecommerce.Product;
import com.ecommerce.db.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();

        String query = """
                SELECT product_id, name, price
                FROM products
                ORDER BY CAST(SUBSTRING(product_id FROM 6) AS INTEGER)
                """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getString("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price")
                );
                products.add(product);
            }
        }

        return products;
    }

    public Product getProductById(String productId) throws SQLException {
        String query = """
            SELECT product_id, name, price
            FROM products
            WHERE LOWER(product_id) = LOWER(?)
            """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getString("product_id"),
                            rs.getString("name"),
                            rs.getDouble("price")
                    );
                }
            }
        }

        return null;
    }
}
