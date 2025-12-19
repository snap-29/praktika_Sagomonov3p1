package com.example.crudapp.service;

import com.example.crudapp.dao.DatabaseProductDAO;
import com.example.crudapp.dao.ProductDAO;
import com.example.crudapp.model.Product;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ProductService {
    private ProductDAO productDAO;

    // Конструктор по умолчанию для реального использования
    public ProductService() {
        this.productDAO = createProductDAO();
    }

    // Конструктор для инъекции зависимости (используется в тестах)
    protected ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    // Метод для создания DAO (можно переопределить в тестах)
    protected ProductDAO createProductDAO() {
        return new DatabaseProductDAO();
    }

    public void createProduct(String name, String description) throws IllegalArgumentException, SQLException {
        if (name == null || name.trim().length() < 3 || name.trim().length() > 50) {
            throw new IllegalArgumentException("Название должно содержать от 3 до 50 символов");
        }

        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("Описание не должно превышать 255 символов");
        }

        Product product = new Product(name.trim(), description != null ? description.trim() : null);
        productDAO.save(product);
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.findAll();
    }

    public List<Product> getProductsWithPagination(int page, int pageSize) throws SQLException {
        int offset = (page - 1) * pageSize;
        return productDAO.findWithPagination(offset, pageSize);
    }

    public List<Product> searchProductsByName(String name) throws SQLException {
        return productDAO.findByName(name);
    }

    public Product getProductById(UUID id) throws SQLException {
        return productDAO.findById(id);
    }

    public void updateProduct(Product product) throws IllegalArgumentException, SQLException {
        if (product.getName() == null || product.getName().trim().length() < 3 ||
                product.getName().trim().length() > 50) {
            throw new IllegalArgumentException("Название должно содержать от 3 до 50 символов");
        }

        if (product.getDescription() != null && product.getDescription().length() > 255) {
            throw new IllegalArgumentException("Описание не должно превышать 255 символов");
        }

        // Обрезаем пробелы
        product.setName(product.getName().trim());
        if (product.getDescription() != null) {
            product.setDescription(product.getDescription().trim());
        }

        productDAO.update(product);
    }

    public void deleteProduct(UUID id) throws SQLException {
        productDAO.delete(id);
    }

    public int getTotalProductCount() throws SQLException {
        return productDAO.countAll();
    }

    public int getTotalPages(int pageSize) throws SQLException {
        int totalCount = getTotalProductCount();
        if (totalCount == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    // Метод для тестирования (чтобы можно было проверить состояние DAO)
    protected ProductDAO getProductDAO() {
        return productDAO;
    }

    // Метод для сброса DAO (для тестов)
    protected void setProductDAO(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }
}