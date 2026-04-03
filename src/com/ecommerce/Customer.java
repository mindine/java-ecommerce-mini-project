package com.ecommerce;

import com.ecommerce.orders.Order;

import java.util.LinkedHashMap;
import java.util.Map;

public class Customer {
    private final String customerID;
    private final String name;
    private final Map<Product, Integer> cart = new LinkedHashMap<>();

    public Customer(String customerID, String name) {
        if (customerID == null || customerID.isBlank()) throw new IllegalArgumentException("customerID is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (name.trim().length() < 3) throw new IllegalArgumentException("Name must be at least 3 characters");

        this.customerID = customerID.trim();
        this.name = name.trim();
    }

    @Override
    public String toString() {
        return customerID + " " + name;
    }

    public String getCustomerID() { return customerID; }
    public String getName() { return name; }

    public Map<Product, Integer> getCart() {
        return cart;
    }

    public void addToCart(Product product, int quantity) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        cart.put(product, cart.getOrDefault(product, 0) + quantity);
    }

    public void removeFromCart(Product product, int quantity) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");

        Integer current = cart.get(product);
        if (current == null) throw new IllegalArgumentException("Product not in cart");

        int newQty = current - quantity;
        if (newQty > 0) cart.put(product, newQty);
        else cart.remove(product);
    }

    public double calculateCartTotal() {
        double total = 0.0;
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            total += entry.getKey().price() * entry.getValue();
        }
        return total;
    }

    public void clearCart() {
        cart.clear();
    }

    public Order placeOrder() {
        if (cart.isEmpty()) throw new IllegalStateException("Cannot place an order with an empty cart");

        String sub = name.length() >= 3 ? name.substring(0, 3).toUpperCase() : name.toUpperCase();
        String orderID = "eOID-" + sub + "-" + (int) (Math.random() * 10000);

        double total = calculateCartTotal();
        return new Order(orderID, this, cart, total);
    }
}