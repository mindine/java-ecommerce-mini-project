package com.ecommerce.files;

import com.ecommerce.orders.Order;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ReceiptWriter {
    public static void writeReceipt(Order order) throws IOException {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        String fileName = "src/com/ecommerce/files/receipt_" + order.getOrderID() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(order.generateSummary());
        }
    }
}
