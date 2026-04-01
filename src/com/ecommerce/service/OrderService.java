package com.ecommerce.service;

import com.ecommerce.Customer;
import com.ecommerce.db.dao.OrderDao;
import com.ecommerce.files.ReceiptWriter;
import com.ecommerce.network.OrderNotificationClient;
import com.ecommerce.orders.Order;

import java.io.IOException;
import java.sql.SQLException;

public class OrderService {
    public Order checkout(Customer customer) throws IllegalAccessException, SQLException, IOException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        if (customer.getCart().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Add items first.");
        }

        Order order = customer.placeOrder();

        // PostgreSQL persistence
        new OrderDao().saveOrder(order);

        // Network Notification
        OrderNotificationClient.sendOrderNotification(order);

        // receipt writer
        ReceiptWriter.writeReceipt(order);

        // clear cart only after all steps succeeded
        customer.getCart().clear();

        return order;
    }
}
