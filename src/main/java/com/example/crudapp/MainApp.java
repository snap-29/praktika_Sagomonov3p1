package com.example.crudapp;

import com.example.crudapp.dao.DatabaseConnection;
import com.example.crudapp.controller.MainController;
import javax.swing.*;

public class MainApp {

    public static void main(String[] args) {
        
        DatabaseConnection.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            try {
                MainController controller = new MainController();
                controller.showMainWindow();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Ошибка запуска приложения: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnection.closeConnection();
        }));
    }
}
