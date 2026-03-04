package com.ecommerce;

import java.util.Objects;

public record Product(String productID, String name, double price) {

    public Product {
        if (productID == null || productID.isBlank()) throw new IllegalArgumentException("productID is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (price < 0) throw new IllegalArgumentException("price cannot be negative");

        productID = productID.trim();
        name = name.trim();
    }

    @Override
    public String toString() {
        return productID + " - " + name + " ($" + String.format("%.2f", price) + ")";
    }

    // Use productID for equality so Product works well as a HashMap key.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return productID.equals(product.productID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productID);
    }
}