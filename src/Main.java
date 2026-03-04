import com.ecommerce.Customer;
import com.ecommerce.Product;
import com.ecommerce.db.Db;
import com.ecommerce.db.DbConfig;
import com.ecommerce.db.dao.OrderDao;
import com.ecommerce.orders.Order;
import com.ecommerce.storage.SavedSession;
import com.ecommerce.storage.SavedSessions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("WELCOME TO OUR E-COMMERCE PLATFORM");
        System.out.println("=================================");

        Customer customer = createCustomer();

        List<Product> catalog = buildCatalog();
        System.out.println("\nHello, " + customer.getName() + "! Browse our products:\n");
        displayCatalog(catalog);

        runShopping(customer, catalog);
    }

    private static Customer createCustomer() {
        while (true) {
            System.out.print("Enter your name to start shopping: ");
            String name = SCANNER.nextLine().trim();

            try {
                String customerID = "eCID-" + (int) (Math.random() * 10000);
                return new Customer(customerID, name);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid name: " + e.getMessage());
            }
        }
    }

    private static List<Product> buildCatalog() {
        List<Product> catalog = new ArrayList<>();
        catalog.add(new Product("ePID-1", "Laptop", 1000));
        catalog.add(new Product("ePID-2", "Smartphone", 800));
        catalog.add(new Product("ePID-3", "Headphones", 150));
        catalog.add(new Product("ePID-4", "Camera", 500));
        catalog.add(new Product("ePID-5", "Smartwatch", 200));
        catalog.add(new Product("ePID-6", "Tablet", 300));
        catalog.add(new Product("ePID-7", "Gaming Console", 400));
        catalog.add(new Product("ePID-8", "Bluetooth Speaker", 100));
        catalog.add(new Product("ePID-9", "External Hard Drive", 120));
        catalog.add(new Product("ePID-10", "Wireless Charger", 50));
        return catalog;
    }

    private static void displayCatalog(List<Product> catalog) {
        for (Product p : catalog) {
            System.out.println(p);
        }
    }

    private static void runShopping(Customer customer, List<Product> catalog) {
        while (true) {
            System.out.println("\nMENU");
            System.out.println("1) Add product to cart");
            System.out.println("2) Remove product from cart");
            System.out.println("3) View cart and total");
            System.out.println("4) Place order (save session)");
            System.out.println("5) View saved sessions");
            System.out.println("6) Delete session by Order ID");
            System.out.println("7) Clear ALL sessions");
            System.out.println("8) Exit");
            System.out.print("Choose an option (1-8): ");

            String line = SCANNER.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number (1-8).");
                continue;
            }

            try {
                switch (choice) {
                    case 1 -> addToCartFlow(customer, catalog);
                    case 2 -> removeFromCartFlow(customer, catalog);
                    case 3 -> viewCart(customer);
                    case 4 -> placeOrderFlow(customer);
                    case 5 -> {
                        SavedSessions.viewAllSessions();
                        var orders = new OrderDao().printAllOrders();
                        for (Order o : orders) {
                            System.out.println(o.toString());
                        }
                    }
                    case 6 -> deleteSessionFlow();
                    case 7 -> clearSessionsFlow();
                    case 8 -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    case 9 -> {
                        // only for backend engineer's connection testing
                        try (var conn = Db.getConnection()) {
                            if (DbConfig.URL == null || DbConfig.USER == null || DbConfig.PASS == null) {
                                throw new IllegalStateException("Database environment variables not set.");
                            }
                            else System.out.println("Connected to database: " + (conn != null && !conn.isClosed()));
                        }
                    }
                    default -> System.out.println("Choice must be between 1 and 8.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void addToCartFlow(Customer customer, List<Product> catalog) {
        Product p = pickProductById(catalog);
        int qty = readPositiveInt("Enter quantity: ");
        customer.addToCart(p, qty);
        System.out.println(qty + " x " + p.name() + " added to cart.");
    }

    private static void removeFromCartFlow(Customer customer, List<Product> catalog) {
        if (customer.getCart().isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        Product p = pickProductById(catalog);
        int qty = readPositiveInt("Enter quantity to remove: ");
        customer.removeFromCart(p, qty);
        System.out.println("Removed items. Cart updated.");
    }

    private static void viewCart(Customer customer) {
        if (customer.getCart().isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        System.out.println("\nCART");
        System.out.println("----------------------------------");
        customer.getCart().forEach((product, qty) ->
                System.out.println(qty + " x " + product.name() + " @ $" + String.format("%.2f", product.price()))
        );
        System.out.println("----------------------------------");
        System.out.println("Total: $" + String.format("%.2f", customer.calculateCartTotal()));
    }

    private static void placeOrderFlow(Customer customer) throws SQLException {
        if (customer.getCart().isEmpty()) {
            System.out.println("Cart is empty. Add items first.");
            return;
        }

        Order order = customer.placeOrder();

        // csv implementation
        SavedSession session = new SavedSession(order);
        SavedSessions.saveSession(session);

        // postgres database implementation
        new OrderDao().saveOrder(order);

        System.out.println(order.generateSummary());
        System.out.println("Order placed and session saved successfully.");

        customer.getCart().clear();
    }

    private static void deleteSessionFlow() throws SQLException {
        System.out.print("Enter Order ID to delete (e.g., eOID-XXX-1234): ");
        String orderId = SCANNER.nextLine().trim();

        if (orderId.isBlank()) {
            System.out.println("Order ID cannot be empty.");
            return;
        }

        boolean sqlDelete = new OrderDao().deleteOrderById(orderId);
        boolean csvDelete = SavedSessions.deleteByOrderId(orderId);
        if (csvDelete && sqlDelete) System.out.println("Session deleted from CSV && PosgreSQL successfully.");
        else System.out.println("Order ID not found. Nothing sqlDelete && csvDelete.");
    }

    private static void clearSessionsFlow() throws SQLException {
        System.out.print("Are you sure you want to clear ALL sessions? (yes/no): ");
        String ans = SCANNER.nextLine().trim();

        while (!ans.equalsIgnoreCase("yes") && !ans.equalsIgnoreCase("no")) {
            System.out.print("Please type yes or no: ");
            ans = SCANNER.nextLine().trim();
        }

        if (ans.equalsIgnoreCase("yes")) {
            new OrderDao().clearAllOrders();
            SavedSessions.clearAll();
            System.out.println("All sessions cleared.");
        } else {
            System.out.println("Cancelled.");
        }
    }

    private static Product pickProductById(List<Product> catalog) {
        while (true) {
            System.out.print("Enter product ID (ePID-1 ... ePID-10): ");
            String id = SCANNER.nextLine().trim();

            for (Product p : catalog) {
                if (p.productID().equalsIgnoreCase(id)) {
                    return p;
                }
            }
            System.out.println("Product not found. Try again.");
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value > 0) return value;
                System.out.println("Value must be > 0.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }
}