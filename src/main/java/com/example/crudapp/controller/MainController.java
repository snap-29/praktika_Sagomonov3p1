package com.example.crudapp.controller;

import com.example.crudapp.model.Product;
import com.example.crudapp.service.ProductService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainController {

    private final ProductService productService;
    private JFrame mainFrame;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JLabel pageInfoLabel;
    private JTextField searchField;
    private JButton prevButton, nextButton;
    private JComboBox<String> pageSizeComboBox;

    private JButton createButton;
    private JButton readButton;
    private JButton updateButton;
    private JButton deleteButton;
    
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
        mainFrame.setSize(1200, 800); // Увеличил размер для лучшего отображения
        mainFrame.setLocationRelativeTo(null);

        createUI();
        loadProducts();
        mainFrame.setVisible(true);
    }

    private void createUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Управление продуктами", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel crudPanel = createCRUDPanel();

        JPanel searchPanel = createSearchPanel();

        JPanel tablePanel = createTablePanel();

        JPanel paginationPanel = createPaginationPanel();

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(crudPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(tablePanel), BorderLayout.CENTER);
        mainPanel.add(paginationPanel, BorderLayout.SOUTH);

        mainFrame.add(mainPanel);
    }

    private JPanel createCRUDPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBorder(new TitledBorder("CRUD Операции"));
        
        // Кнопка CREATE (Создание)
        createButton = new JButton("Создать продукт");
        createButton.setBackground(new Color(39, 174, 96)); // Зеленый
        createButton.setForeground(Color.WHITE);
        createButton.setFont(new Font("Arial", Font.BOLD, 14));
        createButton.setPreferredSize(new Dimension(180, 40));
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCreateProduct();
            }
        });
        
        readButton = new JButton("Обновить список");
        readButton.setBackground(new Color(52, 152, 219)); // Синий
        readButton.setForeground(Color.WHITE);
        readButton.setFont(new Font("Arial", Font.BOLD, 14));
        readButton.setPreferredSize(new Dimension(180, 40));
        readButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleReadProducts();
            }
        });
        
        updateButton = new JButton("Редактировать");
        updateButton.setBackground(new Color(241, 196, 15)); // Желтый/Оранжевый
        updateButton.setForeground(Color.BLACK);
        updateButton.setFont(new Font("Arial", Font.BOLD, 14));
        updateButton.setPreferredSize(new Dimension(180, 40));
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUpdateProduct();
            }
        });
        updateButton.setEnabled(false); // Изначально отключена
        
        deleteButton = new JButton("Удалить продукт");
        deleteButton.setBackground(new Color(231, 76, 60)); // Красный
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteButton.setPreferredSize(new Dimension(180, 40));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeleteProduct();
            }
        });
        deleteButton.setEnabled(false);а
        
        panel.add(createButton);
        panel.add(readButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBorder(new TitledBorder("Поиск"));
        panel.setPreferredSize(new Dimension(400, 70));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setToolTipText("Введите название для поиска...");

        JButton searchButton = new JButton("Найти");
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));
        searchButton.addActionListener(e -> handleSearch());

        JButton resetButton = new JButton("Сбросить");
        resetButton.setFont(new Font("Arial", Font.BOLD, 12));
        resetButton.addActionListener(e -> handleResetSearch());

        panel.add(new JLabel("Поиск:"));
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(resetButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Список продуктов"));

        String[] columns = {"ID", "Название", "Описание", "Дата создания", "Дата обновления"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        productTable = new JTable(tableModel);
        productTable.setFillsViewportHeight(true);
        productTable.setRowHeight(30);
        productTable.setFont(new Font("Arial", Font.PLAIN, 12));
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        productTable.getTableHeader().setBackground(new Color(44, 62, 80));
        productTable.getTableHeader().setForeground(Color.WHITE);
        
        productTable.getColumnModel().getColumn(0).setPreferredWidth(250); // ID
        productTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Название
        productTable.getColumnModel().getColumn(2).setPreferredWidth(300); // Описание
        productTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Дата создания
        productTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Дата обновления

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = productTable.getSelectedRow() != -1;
                updateButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
                
                if (hasSelection) {
                    int selectedRow = productTable.getSelectedRow();
                    String productName = (String) tableModel.getValueAt(selectedRow, 1);
                    System.out.println("Выбран продукт: " + productName);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setPreferredSize(new Dimension(1150, 450));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder("Навигация"));
        panel.setPreferredSize(new Dimension(1150, 70));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        prevButton = new JButton("← Предыдущая");
        prevButton.setFont(new Font("Arial", Font.BOLD, 12));
        prevButton.addActionListener(e -> handlePreviousPage());
        prevButton.setEnabled(false);

        pageInfoLabel = new JLabel("Загрузка...");
        pageInfoLabel.setFont(new Font("Arial", Font.BOLD, 12));

        nextButton = new JButton("Следующая →");
        nextButton.setFont(new Font("Arial", Font.BOLD, 12));
        nextButton.addActionListener(e -> handleNextPage());
        nextButton.setEnabled(false);

        navPanel.add(prevButton);
        navPanel.add(pageInfoLabel);
        navPanel.add(nextButton);

        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        sizePanel.add(new JLabel("Элементов на странице:"));
        pageSizeComboBox = new JComboBox<>(new String[]{"5", "10", "20", "50"});
        pageSizeComboBox.setSelectedItem("10");
        pageSizeComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        pageSizeComboBox.addActionListener(e -> handlePageSizeChange());

        sizePanel.add(pageSizeComboBox);

        panel.add(navPanel, BorderLayout.WEST);
        panel.add(sizePanel, BorderLayout.EAST);

        return panel;
    }

    private void handleCreateProduct() {
        System.out.println("Создание нового продукта...");
        showProductDialog(null); // null означает создание нового
    }

    private void handleReadProducts() {
        System.out.println("Обновление списка продуктов...");
        currentPage = 1; // Возвращаемся на первую страницу
        loadProducts();
        JOptionPane.showMessageDialog(mainFrame, 
            "Список продуктов обновлен!", 
            "Информация", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleUpdateProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            try {
                String productId = (String) tableModel.getValueAt(selectedRow, 0);
                Product product = productService.getProductById(java.util.UUID.fromString(productId));
                if (product != null) {
                    System.out.println("Редактирование продукта: " + product.getName());
                    showProductDialog(product);
                }
            } catch (Exception e) {
                showError("Ошибка", "Ошибка получения продукта: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(mainFrame, 
                "Выберите продукт для редактирования!", 
                "Внимание", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleDeleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            String productId = (String) tableModel.getValueAt(selectedRow, 0);
            String productName = (String) tableModel.getValueAt(selectedRow, 1);

            int response = JOptionPane.showConfirmDialog(mainFrame,
                    "Вы уверены, что хотите удалить продукт:\n" +
                    "\"" + productName + "\"?\n\n" +
                    "Это действие нельзя отменить!",
                    "Подтверждение удаления", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                try {
                    productService.deleteProduct(java.util.UUID.fromString(productId));
                    System.out.println("Удален продукт: " + productName);
                    loadProducts();
                    
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Продукт \"" + productName + "\" успешно удален!", 
                        "Успех", 
                        JOptionPane.INFORMATION_MESSAGE);
                        
                } catch (SQLException e) {
                    showError("Ошибка", "Ошибка удаления продукта: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(mainFrame, 
                "Выберите продукт для удаления!", 
                "Внимание", 
                JOptionPane.WARNING_MESSAGE);
        }
    }


    private void loadProducts() {
        try {
            tableModel.setRowCount(0);

            List<Product> products;
            if (currentSearch.isEmpty()) {
                products = productService.getProductsWithPagination(currentPage, pageSize);
                updatePageInfo();
            } else {
                products = productService.searchProductsByName(currentSearch);
                updatePageInfoForSearch(products.size());
            }

            for (Product product : products) {
                tableModel.addRow(new Object[]{
                        product.getId().toString(),
                        product.getName(),
                        product.getDescription(),
                        product.getCreatedAt().format(formatter),
                        product.getUpdatedAt().format(formatter)
                });
            }
            
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);

        } catch (SQLException e) {
            showError("Ошибка загрузки данных", "Ошибка загрузки данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePageInfo() {
        try {
            int totalProducts = productService.getTotalProductCount();
            int totalPages = productService.getTotalPages(pageSize);

            pageInfoLabel.setText(String.format("Страница %d из %d (Всего: %d продуктов)",
                    currentPage, totalPages, totalProducts));

            prevButton.setEnabled(currentPage > 1);
            nextButton.setEnabled(currentPage < totalPages);

        } catch (SQLException e) {
            showError("Ошибка", "Ошибка получения информации о страницах");
        }
    }

    private void updatePageInfoForSearch(int searchCount) {
        pageInfoLabel.setText(String.format("Результаты поиска: найдено %d элементов", searchCount));
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
    }

    private void handleSearch() {
        currentSearch = searchField.getText().trim();
        currentPage = 1;
        System.out.println("Поиск: " + currentSearch);
        loadProducts();
    }

    private void handleResetSearch() {
        searchField.setText("");
        currentSearch = "";
        currentPage = 1;
        System.out.println("Сброс поиска");
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
            System.out.println("Изменен размер страницы: " + pageSize);
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
                    productService.createProduct(
                            updatedProduct.getName(),
                            updatedProduct.getDescription()
                    );
                    System.out.println("Создан новый продукт: " + updatedProduct.getName());
                } else {

                    updatedProduct.setId(product.getId());
                    updatedProduct.setCreatedAt(product.getCreatedAt());
                    productService.updateProduct(updatedProduct);
                    System.out.println("Обновлен продукт: " + updatedProduct.getName());
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
