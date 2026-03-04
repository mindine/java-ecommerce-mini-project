package com.ecommerce.storage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SavedSessions {

    // Relative path = portable. You can move project and it still works.
    private static final String FILE_PATH = "src/com/ecommerce/storage/sessions.csv";

    // CREATE: append one session
    public static void saveSession(SavedSession session) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(session.toCsvRow());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving session: " + e.getMessage());
        }
    }

    // READ: load all sessions
    public static List<SavedSession> loadAllSessions() {
        List<SavedSession> sessions = new ArrayList<>();

        File f = new File(FILE_PATH);
        if (!f.exists()) return sessions; // no file yet = no sessions

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    sessions.add(SavedSession.fromCsvRow(line));
                } catch (Exception e) {
                    System.out.println("Skipping corrupted row: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading sessions: " + e.getMessage());
        }

        return sessions;
    }

    // VIEW: print all sessions
    public static void viewAllSessions() {
        List<SavedSession> sessions = loadAllSessions();
        if (sessions.isEmpty()) {
            System.out.println("No saved sessions found.");
            return;
        }

        System.out.println("\nSAVED SESSIONS");
        System.out.println("====================================");
        for (SavedSession s : sessions) {
            System.out.println(s);
        }
        System.out.println("====================================");
    }

    // DELETE: delete by orderID
    public static boolean deleteByOrderId(String orderId) {
        List<SavedSession> sessions = loadAllSessions();
        int before = sessions.size();

        sessions.removeIf(s -> s.getOrderID().equalsIgnoreCase(orderId));

        if (sessions.size() == before) return false; // nothing removed

        rewriteAll(sessions);
        return true;
    }

    // CLEAR: wipe everything
    public static void clearAll() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            // overwrite with nothing
        } catch (IOException e) {
            System.out.println("Error clearing sessions: " + e.getMessage());
        }
    }

    // helper: rewrite file
    private static void rewriteAll(List<SavedSession> sessions) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (SavedSession s : sessions) {
                writer.write(s.toCsvRow());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error rewriting sessions file: " + e.getMessage());
        }
    }
}