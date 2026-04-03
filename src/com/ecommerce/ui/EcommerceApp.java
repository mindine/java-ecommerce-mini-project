package com.ecommerce.ui;

import com.ecommerce.Customer;
import com.ecommerce.Product;
import com.ecommerce.network.OrderNotificationServer;
import com.ecommerce.orders.Order;
import com.ecommerce.service.CatalogService;
import com.ecommerce.service.OrderHistoryService;
import com.ecommerce.service.OrderService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EcommerceApp extends Application {

    private Customer currentCustomer;
    private Button checkoutBtn;
    private OrderNotificationServer notificationServer;
    Thread serverThread;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        notificationServer = new OrderNotificationServer();
        serverThread = new Thread(notificationServer);
        serverThread.setName("order-notification-server");
        serverThread.setUncaughtExceptionHandler((thread, ex) -> {
            System.out.println("server thread crashed: " + ex.getMessage());
            ex.printStackTrace();
        });
        serverThread.start();

        try {
            Thread.sleep(300);
        } catch (Exception e) {
            System.out.println("Server startup wait interrupted.");
        }

        // Layouts
        stage.setTitle("My E-commerce Platform");
        stage.setScene(createWelcomeScene(stage));
        stage.show();
    }

    private Scene createWelcomeScene(Stage stage) {
        Label title = new Label("Welcome to Ecommerce Platform!");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label nameLabel = new Label("Enter your name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Customer Name");

        Label messageLabel = new Label();

        Button continueBtn = new Button("Continue");
        Button exitBtn = new Button("Exit");

        continueBtn.setOnAction(event -> {
            String name = nameField.getText().trim();

            if (name.isEmpty()) {
                messageLabel.setText("Please enter your name");
                return;
            }

            String customerId = "eCID-" + (int) (Math.random() * 10000);
            currentCustomer = new Customer(customerId, name);

            stage.setScene(createStoreScene(stage));
        });

        exitBtn.setOnAction(event -> stage.close());

        VBox root = new VBox(15, title, nameLabel, nameField, continueBtn, exitBtn,  messageLabel);
        root.setPadding(new Insets(20));

        return new Scene(root, 500, 300);
    }

    private Scene createOrderHistoryScene(Stage stage) {
        Label title = new Label("Order History");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextArea ordersArea = new TextArea();
        ordersArea.setEditable(false);
        ordersArea.setWrapText(true);

        Button refreshBtn = new Button("Refresh");
        Button backBtn = new Button("Back to Store");
        Button clearHistoryBtn = new Button("Clear History");
        Button exitBtn = new Button("Exit");

        OrderHistoryService historyService = new OrderHistoryService();

        Runnable loadOrders = () -> {
            try {
                var orders = historyService.getAllOrders();

                if (orders.isEmpty()) {
                    ordersArea.setText("There are no orders in the database.");
                } else {
                    StringBuilder sb = new StringBuilder();

                    for (Order order : orders) {
                        sb.append(order.generateSummary()).append("\n\n");
                    }

                    ordersArea.setText(sb.toString());
                }
            }  catch (Exception ex) {
                ordersArea.setText("Error loading orders: " + ex.getMessage());
            }
        };

        // Handler for clear history
        clearHistoryBtn.setOnAction(event -> {
            Alert confirm =  new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Clear History");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to clear all order history?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        historyService.clearAllOrders();
                        ordersArea.setText("Order history cleared.");
                    } catch (Exception ex) {
                        ordersArea.setText("Error loading orders: " + ex.getMessage());
                    }
                }
            });
        });

        refreshBtn.setOnAction(e -> loadOrders.run());

        backBtn.setOnAction(e -> stage.setScene(createStoreScene(stage)));

        exitBtn.setOnAction(event -> stage.close());

        loadOrders.run();

        HBox buttons = new HBox(10, refreshBtn, clearHistoryBtn, backBtn, exitBtn);
        VBox root = new VBox(15, title, ordersArea, buttons);
        root.setPadding(new Insets(15));

        return new Scene(root, 800, 500);
    }

    private Scene createStoreScene(Stage stage) {
        CatalogService catalogService = new CatalogService();
        OrderService orderService = new OrderService();

        // Title
        Label title = new Label("Welcome to My E-commerce Platform!");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Product list
        ListView<Product> productListView = new ListView<>();

        try {
            productListView.getItems().addAll(catalogService.loadCatalog());
            System.out.println("DB connection OK. Products loaded: " + productListView.getItems().size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DB connection failed: " + e.getMessage());
        }

        productListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                } else {
                    setText(p.productID() + " _ " + p.name() + " ($" + p.price() + ")");
                }
            }
        });

        // Cart display
        TextArea cartArea = new TextArea();
        cartArea.setEditable(false);
        cartArea.setPrefWidth(320);
        cartArea.setPrefHeight(300);
        cartArea.setWrapText(true);

        Label cartLabel = new Label("Cart / Order Details");
        cartLabel.setStyle("-fx-font-weight: bold;");

        VBox rightPanel = new VBox(10, cartLabel, cartArea); // cart update VBox

        // Quantity input
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        // Buttons
        Button addToCartBtn = new Button("Add to cart");
        Button removeFromCartBtn = new Button("Remove from cart");
        Button resetCartBtn = new Button("Reset cart");
        Button exitBtn = new Button("Exit");
        checkoutBtn = new Button("Checkout");
        checkoutBtn.setDisable(true);

        Button viewOrdersBtn = new Button("View Orders");

        // add to Cart button logic
        addToCartBtn.setOnAction(e -> {
            Product selected = productListView.getSelectionModel().getSelectedItem();

            if (selected == null) {
                cartArea.setText("Please select a product to cart!");
                return;
            }

            int qty;
            try {
                qty = Integer.parseInt(quantityField.getText().trim());
                if (qty <= 0) {
                    cartArea.setText("Quantity must be greater than 0.");
                    return;
                }
            } catch (NumberFormatException ex) {
                cartArea.setText("Enter a valid number.");
                return;
            }

            currentCustomer.addToCart(selected, qty);
            quantityField.clear();
            updateCartDisplay(currentCustomer, cartArea);
        });

        // remove from cart button
        removeFromCartBtn.setOnAction(e -> {
           Product selected = productListView.getSelectionModel().getSelectedItem();

           if (selected == null) {
               cartArea.setText("Please select a product first");
               return;
           }

           int  qty;
           try {
               qty = Integer.parseInt(quantityField.getText().trim());
               if (qty <= 0) {
                   cartArea.setText("Quantity must be greater than 0.");
                   return;
               }
           } catch (NumberFormatException ex) {
               cartArea.setText("Enter a valid number.");
               return;
           }

           try {
               currentCustomer.removeFromCart(selected, qty);
               quantityField.clear();
               updateCartDisplay(currentCustomer, cartArea);
           } catch (Exception ex) {
               cartArea.setText("Error removing item: " + ex.getMessage());
           }
        });

        // checkout button logic
        checkoutBtn.setOnAction(e -> {
            try {
                Order order = orderService.checkout(currentCustomer);
//                cartArea.setText("Order placed successfully!\n\n" + order.generateSummary());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Order placed successfully!");
                alert.showAndWait();
                cartArea.clear();
                checkoutBtn.setDisable(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                cartArea.setText("Error: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            }
        });

        // reset cart button handler
        resetCartBtn.setOnAction(e -> {
            currentCustomer.clearCart();
            updateCartDisplay(currentCustomer, cartArea);
        });

        // exit button handler
        exitBtn.setOnAction(e -> stage.close());

        viewOrdersBtn.setOnAction(e -> stage.setScene(createOrderHistoryScene(stage)));

        // Layouts
        HBox mainContent = new HBox(20, productListView, rightPanel);
        mainContent.setPadding(new Insets(10));

        HBox controls = new HBox(10, quantityField, addToCartBtn, removeFromCartBtn, resetCartBtn, viewOrdersBtn);
        controls.setPadding(new Insets(10));

        HBox bottomButtons = new HBox(10, checkoutBtn, exitBtn);

        VBox root = new VBox(15, title, mainContent, controls, bottomButtons);
        root.setPadding(new Insets(15));

        return new Scene(root, 800, 500);
    }

    private void updateCartDisplay(Customer customer, TextArea cartArea) {
        StringBuilder sb = new StringBuilder();

        sb.append("CART\n------------------------------------------\n");

        customer.getCart().forEach((p, qty) -> {
            sb.append(String.format("%-5d x %-20s $%.2f\n", qty, p.name(), p.price()));
//            sb.append(qty)
//                    .append(" x ")
//                    .append(p.name())
//                    .append(" ($")
//                    .append(p.price())
//                    .append(")\n");
        });

        sb.append("------------------------------------------\n");
        sb.append("Total: $")
                .append(String.format("%.2f", customer.calculateCartTotal()));


        cartArea.setText(sb.toString());

        checkoutBtn.setDisable(customer.getCart().isEmpty());
    }

    @Override
    public void stop() {
        System.out.println("Shutting down application...");

        if (notificationServer != null) {
            notificationServer.stopServer();
        }
    }
}
