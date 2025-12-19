package com.example.crudapp.controller;

import com.example.crudapp.model.Product;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProductDialogController {

    private JDialog dialog;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JLabel idLabel;
    private JLabel createdAtLabel;
    private JLabel updatedAtLabel;
    private JLabel errorLabel;

    private Product product;
    private boolean result = false;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public ProductDialogController(JFrame parent, Product product) {
        this.product = product;
        createDialog(parent);
    }

    private void createDialog(JFrame parent) {
        dialog = new JDialog(parent, "Редактирование продукта", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(product == null ? "Создание нового продукта" : "Редактирование продукта");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel formPanel = createFormPanel();

        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        if (product != null) {
            nameField.setText(product.getName());
            descriptionArea.setText(product.getDescription());
            idLabel.setText(product.getId().toString());
            createdAtLabel.setText(product.getCreatedAt().format(formatter));
            updatedAtLabel.setText(product.getUpdatedAt().format(formatter));
        } else {
            idLabel.setText("Будет сгенерирован автоматически");
            createdAtLabel.setText("Будет установлена автоматически");
            updatedAtLabel.setText("Будет установлена автоматически");
        }
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Информация о продукте"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Название*:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        nameField = new JTextField(30);
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Описание:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        panel.add(scrollPane, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        idLabel = new JLabel();
        idLabel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(idLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("Дата создания:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        createdAtLabel = new JLabel();
        createdAtLabel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(createdAtLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Дата обновления:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        updatedAtLabel = new JLabel();
        updatedAtLabel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(updatedAtLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(errorLabel, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton saveButton = new JButton("Сохранить");
        saveButton.addActionListener(e -> {
            if (validateInput()) {
                result = true;
                dialog.dispose();
            }
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> {
            result = false;
            dialog.dispose();
        });

        panel.add(saveButton);
        panel.add(cancelButton);

        return panel;
    }

    private boolean validateInput() {
        errorLabel.setText("");

        String name = nameField.getText().trim();
        String description = descriptionArea.getText();

        if (name.isEmpty() || name.length() < 3 || name.length() > 50) {
            errorLabel.setText("Название должно содержать от 3 до 50 символов");
            return false;
        }

        if (description != null && description.length() > 255) {
            errorLabel.setText("Описание не должно превышать 255 символов");
            return false;
        }

        if (product == null) {
            product = new Product(name, description);
        } else {
            product.setName(name);
            product.setDescription(description);
            product.setUpdatedAt(LocalDateTime.now());
        }

        return true;
    }

    public boolean showDialog() {
        dialog.setVisible(true);
        return result;
    }

    public Product getProduct() {
        return product;
    }
}
