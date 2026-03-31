package com.ecommerce.orders;

import com.ecommerce.Customer;
import com.ecommerce.Product;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Order {
    public enum Status { PLACED, CANCELLED }

    private final String orderID;
    private final Customer customer;
    private final Map<Product, Integer> items; // snapshot
    private final double orderTotal;
    private Status status;

    public Order(String orderID, Customer customer, Map<Product, Integer> items, double orderTotal) {
        if (orderID == null || orderID.isBlank()) throw new IllegalArgumentException("orderID is required");
        if (customer == null) throw new IllegalArgumentException("customer is required");
        if (items == null) throw new IllegalArgumentException("items is required");

        this.orderID = orderID.trim();
        this.customer = customer;
        this.items = new LinkedHashMap<>(items);
        this.orderTotal = orderTotal;
        this.status = Status.PLACED;
    }

    public String getOrderID() { return orderID; }
    public Customer getCustomer() { return customer; }
    public Map<Product, Integer> getItems() { return Collections.unmodifiableMap(items); }
    public double getOrderTotal() { return orderTotal; }
    public Status getStatus() { return status; }

    public void updateStatus(Status newStatus) {
        if (newStatus == null) throw new IllegalArgumentException("Status cannot be null");
        this.status = newStatus;
    }

    @Override
    public String toString() {
        return "Order : " +
                orderID + '\'' + " " +
                customer.toString() + " " +
                items + " " +
                orderTotal + " " +
                status;
    }

    public String generateSummary() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        StringBuilder sb = new StringBuilder();
        sb.append("Order Summary for --> ");
        sb.append("Customer: ").append(customer.getName()).append(" (").append(customer.getCustomerID()).append(")\n");
        sb.append("-----------------------------------------------------\n");
        sb.append("Order ID: ").append(orderID).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("-----------------------------------------------------\n");

        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            sb.append(qty).append(" x ").append(p.name())
                    .append(" @ $").append(String.format("%.2f", p.price()))
                    .append(" = $").append(String.format("%.2f", p.price() * qty))
                    .append("\n");
        }

        sb.append("-----------------------------------------------------\n");
        sb.append("TOTAL: $").append(String.format("%.2f", orderTotal)).append("\n");

        sb.append("-----------------------------------------------------\n");
        sb.append(dateTime).append("\n");

        return sb.toString();
    }
}