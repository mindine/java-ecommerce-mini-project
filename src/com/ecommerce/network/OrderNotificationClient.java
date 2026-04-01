package com.ecommerce.network;

import com.ecommerce.orders.Order;

import java.io.PrintWriter;
import java.net.Socket;

public class OrderNotificationClient {
    public static void sendOrderNotification(Order order) throws IllegalAccessException {
        if (order == null) {
            throw new IllegalAccessException ("Order cannot be null");
        }

        String host = "localhost";
        int port = 9000;

        String message = "New order placed: "
                + order.getOrderID()
                + " by "
                + order.getCustomer().getCustomerID()
                + " total=$"
                + String.format("%.2f", order.getOrderTotal());

        try (Socket socket = new Socket(host, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println(message);
        } catch (Exception e) {
            System.out.println("Failed to send order notification: " + e.getMessage());
        }
    }
}
