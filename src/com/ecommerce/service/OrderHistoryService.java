package com.ecommerce.service;

import com.ecommerce.db.dao.OrderDao;
import com.ecommerce.orders.Order;

import java.sql.SQLException;
import java.util.List;

public class OrderHistoryService {
    public List<Order> getAllOrders() throws SQLException {
        return new OrderDao().printAllOrders();
    }

    public boolean deleteOrder(String orderId) throws SQLException {
        return new OrderDao().deleteOrderById(orderId);
    }

    public void clearAllOrders() throws SQLException {
        new OrderDao().clearAllOrders();
    }
}