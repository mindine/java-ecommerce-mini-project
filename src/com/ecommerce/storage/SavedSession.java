package com.ecommerce.storage;

import com.ecommerce.Customer;
import com.ecommerce.Product;
import com.ecommerce.orders.Order;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SavedSession {
    private final String orderID;
    private final String customerID;
    private final String customerName;
    private final Map<Product, Integer> items = new LinkedHashMap<>();
    private final double total;

    // Build session from an Order after checkout
    public SavedSession(Order order) {
        this.orderID = order.getOrderID();
        this.customerID = order.getCustomer().getCustomerID();
        this.customerName = order.getCustomer().getName();
        this.items.putAll(order.getItems()); // snapshot
        this.total = order.getOrderTotal();
    }

    public String getOrderID() { return orderID; }
    public String getCustomerID() { return customerID; }
    public String getCustomerName() { return customerName; }
    public Map<Product, Integer> getItems() { return items; }
    public double getTotal() { return total; }

    // Convert to CSV line
    public String toCsvRow() {
        StringBuilder sb = new StringBuilder();
        sb.append(orderID).append(",");
        sb.append(customerID).append(",");
        sb.append(customerName).append(",");

        Iterator<Map.Entry<Product, Integer>> it = items.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Product, Integer> entry = it.next();
            Product p = entry.getKey();
            int qty = entry.getValue();

            sb.append(p.name()).append(":")
                    .append(qty).append(":")
                    .append(p.productID()).append(":")
                    .append(p.price());

            if (it.hasNext()) sb.append("|");
        }

        sb.append(",");
        sb.append(total);
        return sb.toString();
    }

    // Parse from CSV line
    public static SavedSession fromCsvRow(String row) {
        String[] fields = row.split(",");
        if (fields.length != 5) {
            throw new IllegalArgumentException("Invalid CSV row: " + row);
        }

        String readOrderId = fields[0].trim();
        String readCustomerId = fields[1].trim();
        String readCustomerName = fields[2].trim();
        String itemsBlob = fields[3].trim();
        double readTotal = Double.parseDouble(fields[4].trim());

        Map<Product, Integer> readItems = getProductIntegerMap(itemsBlob);

        // Build a minimal Order to reuse existing constructor
        Customer c = new Customer(readCustomerId, readCustomerName);
        Order o = new Order(readOrderId, c, readItems, readTotal);
        return new SavedSession(o);
    }

    private static Map<Product, Integer> getProductIntegerMap(String itemsBlob) {
        Map<Product, Integer> readItems = new LinkedHashMap<>();

        if (!itemsBlob.isBlank()) {
            String[] itemChunks = itemsBlob.split("\\|"); // IMPORTANT: escaped pipe
            for (String chunk : itemChunks) {
                String[] pv = chunk.split(":");
                if (pv.length != 4) continue;

                String productName = pv[0].trim();
                int qty = Integer.parseInt(pv[1].trim());
                String productId = pv[2].trim();
                double price = Double.parseDouble(pv[3].trim());

                readItems.put(new Product(productId, productName, price), qty);
            }
        }
        return readItems;
    }

    @Override
    public String toString() {
        return orderID + " | " + customerID + " | " + customerName + " | Total: $" + String.format("%.2f", total);
    }
}