package com.ecommerce.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class OrderNotificationServer implements Runnable {

    private volatile boolean running = true;
    private ServerSocket serverSocket;

    @Override
    public void run() {
        int port = 9000;

        System.out.println("Order Notifications Server starting on port " + port + "...");

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server listening for order notifications on port " + port + "...");

            while (running) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(socket.getInputStream()))) {

                    String message = reader.readLine();
                    System.out.println("Received notification: " + message);

                } catch (Exception e) {
                    if (running) {
                        System.out.println("Error handling client connection: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            if (running) {
                System.out.println("Server failed to start: " + e.getMessage());
            }
        }

        System.out.println("Server stopped.");
    }

    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            System.out.println("Error stopping socket: " + e.getMessage());
        }
    }
}