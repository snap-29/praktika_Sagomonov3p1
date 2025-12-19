package com.example.crudapp;

import com.example.crudapp.dao.DatabaseConnection;
import com.example.crudapp.controller.MainController;
import javax.swing.*;

public class MainApp {

    public static void main(String[] args) {
        // Инициализация базы данных
        DatabaseConnection.initializeDatabase();

        // Запуск в потоке событий Swing
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

        // Добавляем обработчик завершения работы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnection.closeConnection();
        }));
    }
}