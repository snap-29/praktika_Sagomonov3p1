package com.example.crudapp.controller;

import com.example.crudapp.model.Product;
import com.example.crudapp.service.ProductService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MainController {

    private final ProductService productService;
    private JFrame mainFrame;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JLabel pageInfoLabel;
    private JTextField searchField;
    private JButton prevButton, nextButton;
    private JComboBox<String> pageSizeComboBox;

    private int currentPage = 1;
    private int pageSize = 10;
    private String currentSearch = "";
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public MainController() {
        this.productService = new ProductService();
    }

    public void showMainWindow() {
        mainFrame = new JFrame("CRUD Приложение - Управление продуктами");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 700);
        mainFrame.setLocationRelativeTo(null);

        createUI();
        loadProducts();
        mainFrame.setVisible(true);
    }

    private void createUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Панель заголовка
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Управление продуктами", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Панель кнопок действий
        JPanel buttonPanel = createButtonPanel();

        // Панель поиска
        JPanel searchPanel = createSearchPanel();

        // Таблица продуктов
        JPanel tablePanel = createTablePanel();

        // Панель пагинации
        JPanel paginationPanel = createPaginationPanel();

        // Собираем верхнюю панель
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(buttonPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        // Собираем все вместе
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(new JScrollPane(tablePanel), BorderLayout.CENTER);
        mainPanel.add(paginationPanel, BorderLayout.SOUTH);

        mainFrame.add(mainPanel);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(new TitledBorder("Действия"));

        // Кнопка "Добавить продукт"
        JButton addButton = new JButton("Добавить продукт");
        addButton.setBackground(new Color(39, 174, 96));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.BOLD, 12));
        addButton.addActionListener(e -> handleAddProduct());

        // Кнопка "Редактировать"
        JButton editButton = new JButton("Редактировать");
        editButton.setBackground(new Color(52, 152, 219));
        editButton.setForeground(Color.WHITE);
        editButton.setFont(new Font("Arial", Font.BOLD, 12));
        editButton.addActionListener(e -> handleEditProduct());
        editButton.setEnabled(false);

        // Кнопка "Удалить"
        JButton deleteButton = new JButton("Удалить");
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Arial", Font.BOLD, 12));
        deleteButton.addActionListener(e -> handleDeleteProduct());
        deleteButton.setEnabled(false);

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);

        // Сохраняем ссылки для обновления состояния
        this.productTable = new JTable();
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean isSelected = productTable.getSelectedRow() != -1;
                editButton.setEnabled(isSelected);
                deleteButton.setEnabled(isSelected);
            }
        });

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setBorder(new TitledBorder("Поиск"));

        searchField = new JTextField(20);
        searchField.setToolTipText("Поиск по названию...");

        JButton searchButton = new JButton("Найти");
        searchButton.addActionListener(e -> handleSearch());

        JButton resetButton = new JButton("Сбросить");
        resetButton.addActionListener(e -> handleResetSearch());

        panel.add(new JLabel("Название:"));
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(resetButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Модель таблицы
        String[] columns = {"ID", "Название", "Описание", "Дата создания", "Дата обновления"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрещаем редактирование
            }
        };

        productTable = new JTable(tableModel);
        productTable.setFillsViewportHeight(true);
        productTable.setRowHeight(25);
        productTable.setFont(new Font("Arial", Font.PLAIN, 12));
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Настройка ширины столбцов
        productTable.getColumnModel().getColumn(0).setPreferredWidth(150); // ID
        productTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Название
        productTable.getColumnModel().getColumn(2).setPreferredWidth(300); // Описание
        productTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Дата создания
        productTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Дата обновления

        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(new TitledBorder("Пагинация"));

        // Левая часть - кнопки навигации
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        prevButton = new JButton("← Назад");
        prevButton.addActionListener(e -> handlePreviousPage());
        prevButton.setEnabled(false);

        pageInfoLabel = new JLabel("Страница 1 из 1");

        nextButton = new JButton("Вперед →");
        nextButton.addActionListener(e -> handleNextPage());
        nextButton.setEnabled(false);

        navPanel.add(prevButton);
        navPanel.add(pageInfoLabel);
        navPanel.add(nextButton);

        // Правая часть - выбор размера страницы
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        sizePanel.add(new JLabel("Элементов на странице:"));

        pageSizeComboBox = new JComboBox<>(new String[]{"5", "10", "20", "50"});
        pageSizeComboBox.setSelectedItem("10");
        pageSizeComboBox.addActionListener(e -> handlePageSizeChange());

        sizePanel.add(pageSizeComboBox);

        panel.add(navPanel, BorderLayout.WEST);
        panel.add(sizePanel, BorderLayout.EAST);

        return panel;
    }

    private void loadProducts() {
        try {
            tableModel.setRowCount(0); // Очищаем таблицу

            List<Product> products;
            if (currentSearch.isEmpty()) {
                products = productService.getProductsWithPagination(currentPage, pageSize);
                updatePageInfo();
            } else {
                products = productService.searchProductsByName(currentSearch);
                updatePageInfoForSearch(products.size());
            }

            // Заполняем таблицу
            for (Product product : products) {
                tableModel.addRow(new Object[]{
                        product.getId().toString(),
                        product.getName(),
                        product.getDescription(),
                        product.getCreatedAt().format(formatter),
                        product.getUpdatedAt().format(formatter)
                });
            }

        } catch (SQLException e) {
            showError("Ошибка загрузки данных", "Ошибка загрузки данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePageInfo() {
        try {
            int totalProducts = productService.getTotalProductCount();
            int totalPages = productService.getTotalPages(pageSize);

            pageInfoLabel.setText(String.format("Страница %d из %d (Всего: %d)",
                    currentPage, totalPages, totalProducts));

            prevButton.setEnabled(currentPage > 1);
            nextButton.setEnabled(currentPage < totalPages);

        } catch (SQLException e) {
            showError("Ошибка", "Ошибка получения информации о страницах");
        }
    }

    private void updatePageInfoForSearch(int searchCount) {
        pageInfoLabel.setText(String.format("Результаты поиска: %d элементов", searchCount));
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
    }

    // Обработчики событий
    private void handleAddProduct() {
        showProductDialog(null);
    }

    private void handleEditProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            try {
                String productId = (String) tableModel.getValueAt(selectedRow, 0);
                Product product = productService.getProductById(java.util.UUID.fromString(productId));
                if (product != null) {
                    showProductDialog(product);
                }
            } catch (Exception e) {
                showError("Ошибка", "Ошибка получения продукта: " + e.getMessage());
            }
        }
    }

    private void handleDeleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            String productId = (String) tableModel.getValueAt(selectedRow, 0);
            String productName = (String) tableModel.getValueAt(selectedRow, 1);

            int response = JOptionPane.showConfirmDialog(mainFrame,
                    "Вы уверены, что хотите удалить продукт '" + productName + "'?",
                    "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                try {
                    productService.deleteProduct(java.util.UUID.fromString(productId));
                    loadProducts();
                } catch (SQLException e) {
                    showError("Ошибка", "Ошибка удаления продукта: " + e.getMessage());
                }
            }
        }
    }

    private void handleSearch() {
        currentSearch = searchField.getText().trim();
        currentPage = 1;
        loadProducts();
    }

    private void handleResetSearch() {
        searchField.setText("");
        currentSearch = "";
        currentPage = 1;
        loadProducts();
    }

    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadProducts();
        }
    }

    private void handleNextPage() {
        try {
            int totalPages = productService.getTotalPages(pageSize);
            if (currentPage < totalPages) {
                currentPage++;
                loadProducts();
            }
        } catch (SQLException e) {
            showError("Ошибка", "Ошибка перехода на следующую страницу");
        }
    }

    private void handlePageSizeChange() {
        try {
            pageSize = Integer.parseInt((String) pageSizeComboBox.getSelectedItem());
            currentPage = 1;
            loadProducts();
        } catch (NumberFormatException e) {
            showError("Ошибка", "Некорректное значение размера страницы");
        }
    }

    private void showProductDialog(Product product) {
        ProductDialogController dialog = new ProductDialogController(mainFrame, product);
        if (dialog.showDialog()) {
            Product updatedProduct = dialog.getProduct();
            try {
                if (product == null) {
                    // Создание нового продукта
                    productService.createProduct(
                            updatedProduct.getName(),
                            updatedProduct.getDescription()
                    );
                } else {
                    // Обновление существующего
                    updatedProduct.setId(product.getId());
                    updatedProduct.setCreatedAt(product.getCreatedAt());
                    productService.updateProduct(updatedProduct);
                }
                loadProducts();
            } catch (Exception e) {
                showError("Ошибка", "Ошибка сохранения продукта: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.ERROR_MESSAGE);
    }
}