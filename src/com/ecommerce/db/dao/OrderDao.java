package com.ecommerce.db.dao;

import com.ecommerce.Customer;
import com.ecommerce.Product;
import com.ecommerce.db.Db;
import com.ecommerce.orders.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class OrderDao {
    public void saveOrder(Order order) throws SQLException {
        String upsertCustomer = """
                INSERT INTO customers(customer_id, name)
                VALUES (?, ?)
                ON CONFLICT (customer_id) DO UPDATE SET name = EXCLUDED.name
                """;

        String insertOrder = """
                INSERT INTO orders(order_id, customer_id, total, status)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (order_id)
                DO UPDATE SET total = EXCLUDED.total, status = EXCLUDED.status
                """;

        String insertItem = """
                INSERT INTO order_items(order_id, product_id, unit_price, quantity)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (order_id,  product_id)
                DO UPDATE SET unit_price = EXCLUDED.unit_price, quantity = EXCLUDED.quantity
                """;

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false); // start transaction

            try  (
                    PreparedStatement psCustomer = conn.prepareStatement(upsertCustomer);
                    PreparedStatement psOrder = conn.prepareStatement(insertOrder);
                    PreparedStatement psItem = conn.prepareStatement(insertItem)
            ) {

                // 1) customer
                psCustomer.setString(1, order.getCustomer().getCustomerID());
                psCustomer.setString(2, order.getCustomer().getName());
                psCustomer.executeUpdate();

                // 2) order
                psOrder.setString(1, order.getOrderID());
                psOrder.setString(2, order.getCustomer().getCustomerID());
                psOrder.setDouble(3, order.getOrderTotal());
                psOrder.setString(4, order.getStatus().name());
                psOrder.executeUpdate();

                // 3) order_items (loop)
                for (Map.Entry<Product, Integer> entry : order.getItems().entrySet()) {
                    Product product = entry.getKey();
                    int  quantity = entry.getValue();

                    psItem.setString(1, order.getOrderID());
                    psItem.setString(2, product.productID());
                    psItem.setDouble(3, product.price());
                    psItem.setInt(4, quantity);

                    psItem.addBatch();
                }
                psItem.executeBatch();

                conn.commit(); // success

            } catch (SQLException e) {
                conn.rollback(); // cancel everything
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    public List<Order> printAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();

        Map<String, Customer> customersByOrder = new LinkedHashMap<>();
        Map<String, Double> totalsByOrder = new LinkedHashMap<>();
        Map<String, Map<Product, Integer>> itemsByOrder = new LinkedHashMap<>();

        String query = """
                    SELECT
                              o.order_id,
                              o.total,
                              c.customer_id,
                              c.name AS customer_name,
                              oi.product_id,
                              p.name AS product_name,
                              oi.unit_price,
                              oi.quantity
                            FROM orders o
                            JOIN customers c ON c.customer_id = o.customer_id
                            JOIN order_items oi ON oi.order_id = o.order_id
                            JOIN products p ON p.product_id = oi.product_id
                            ORDER BY o.created_at DESC, o.order_id, p.name;
            """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String orderId = rs.getString("order_id");

                    // 1) creating container if first time seeing orderId
                    itemsByOrder.putIfAbsent(orderId, new LinkedHashMap<>());

                    // 2) capture order-level stuff once (customer + total)
                    customersByOrder.putIfAbsent(orderId, new Customer(rs.getString("customer_id"), rs.getString("customer_name")));
                    totalsByOrder.putIfAbsent(orderId, rs.getDouble("total"));

                    Product product = new Product(
                            rs.getString("product_id"),
                            rs.getString("product_name"),
                            rs.getDouble("unit_price")
                    );

                    itemsByOrder.get(orderId).put(product, rs.getInt("quantity"));
                }
        }

        for (String  orderId : itemsByOrder.keySet()) {
            orders.add(new Order(
                    orderId,
                    customersByOrder.get(orderId),
                    itemsByOrder.get(orderId),
                    totalsByOrder.get(orderId)
            ));
        }
        return orders;
    }
    public boolean deleteOrderById(String orderId) throws SQLException {
        String deleteOrder = "DELETE FROM orders WHERE order_id = ?";

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteOrder)) {

            ps.setString(1, orderId);
            int rowsAffected = ps.executeUpdate();

            return rowsAffected > 0;
        }
    }
    public void clearAllOrders() throws SQLException {
        String deleteAllOrders = "DELETE FROM orders";

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteAllOrders)) {

            ps.executeUpdate();
        }
    }
}
