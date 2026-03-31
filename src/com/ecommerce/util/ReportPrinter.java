package com.ecommerce.util;

import java.util.List;

public class ReportPrinter<T> {
    public void printList(String title, List<T> items) {
        System.out.println("\n" + title);
        System.out.println("#####################################################");

        if (items == null || items.isEmpty()) {
            System.out.println("No items to display.");
            return;
        }

        for (T item : items) {
            System.out.print(item + "\n");
        }

        System.out.println("#####################################################");
    }
}
