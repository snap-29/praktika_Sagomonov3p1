package com.example.crudapp;

import com.example.crudapp.dao.ProductDAO;
import com.example.crudapp.model.Product;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.crudapp.ProductTest;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductDAOTest {

    // Мок DAO, работающий с коллекцией в памяти
    private static class TestProductDAO extends ProductDAO {
        private final List<Product> products = new ArrayList<>();

        @Override
        public void save(Product product) {
            products.add(product);
        }

        @Override
        public List<Product> findAll() {
            return new ArrayList<>(products);
        }

        @Override
        public List<Product> findWithPagination(int offset, int limit) {
            List<Product> result = new ArrayList<>();
            int start = Math.max(0, Math.min(offset, products.size()));
            int end = Math.min(start + limit, products.size());

            for (int i = start; i < end; i++) {
                result.add(products.get(i));
            }
            return result;
        }

        @Override
        public List<Product> findByName(String name) {
            List<Product> result = new ArrayList<>();
            for (Product product : products) {
                if (product.getName().toLowerCase().contains(name.toLowerCase())) {
                    result.add(product);
                }
            }
            return result;
        }

        @Override
        public Product findById(UUID id) {
            return products.stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void update(Product product) {
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId().equals(product.getId())) {
                    products.set(i, product);
                    return;
                }
            }
        }

        @Override
        public void delete(UUID id) {
            products.removeIf(p -> p.getId().equals(id));
        }

        @Override
        public int countAll() {
            return products.size();
        }

        @Override
        protected Connection getConnection() {
            return null; // Не используется в тестах
        }
    }

    private ProductDAO productDAO;

    @BeforeEach
    void setUp() {
        productDAO = new TestProductDAO();
    }

    @Test
    @Order(1)
    void testSaveProduct() throws SQLException {
        Product product = new Product("Test Product", "Test Description");
        productDAO.save(product);

        Product retrieved = productDAO.findById(product.getId());
        assertNotNull(retrieved);
        assertEquals("Test Product", retrieved.getName());
        assertEquals("Test Description", retrieved.getDescription());
    }

    @Test
    @Order(2)
    void testFindAllProducts() throws SQLException {
        productDAO.save(new Product("Product 1", "Desc 1"));
        productDAO.save(new Product("Product 2", "Desc 2"));

        List<Product> products = productDAO.findAll();
        assertEquals(2, products.size());
    }

    @Test
    @Order(3)
    void testFindById() throws SQLException {
        Product product = new Product("Test", "Desc");
        productDAO.save(product);

        Product found = productDAO.findById(product.getId());
        assertEquals(product.getId(), found.getId());
    }

    @Test
    @Order(4)
    void testFindByIdNotFound() throws SQLException {
        assertNull(productDAO.findById(UUID.randomUUID()));
    }

    @Test
    @Order(5)
    void testUpdateProduct() throws SQLException {
        Product product = new Product("Old", "Old Desc");
        productDAO.save(product);

        product.setName("New");
        product.setDescription("New Desc");
        productDAO.update(product);

        Product updated = productDAO.findById(product.getId());
        assertEquals("New", updated.getName());
    }

    @Test
    @Order(6)
    void testDeleteProduct() throws SQLException {
        Product product = new Product("ToDelete", "Desc");
        productDAO.save(product);

        productDAO.delete(product.getId());
        assertNull(productDAO.findById(product.getId()));
    }

    @Test
    @Order(7)
    void testFindByName() throws SQLException {
        productDAO.save(new Product("Apple iPhone", "Phone"));
        productDAO.save(new Product("Apple MacBook", "Laptop"));

        List<Product> results = productDAO.findByName("apple");
        assertEquals(2, results.size());
    }

    @Test
    @Order(8)
    void testCountAll() throws SQLException {
        assertEquals(0, productDAO.countAll());
        productDAO.save(new Product("Test", "Desc"));
        assertEquals(1, productDAO.countAll());
    }

    @Test
    @Order(9)
    void testFindWithPagination() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            productDAO.save(new Product("Product " + i, "Desc " + i));
        }

        List<Product> page1 = productDAO.findWithPagination(0, 2);
        assertEquals(2, page1.size());

        List<Product> page2 = productDAO.findWithPagination(2, 2);
        assertEquals(2, page2.size());
    }

    @Test
    @Order(10)
    void testSaveProductWithNullDescription() throws SQLException {
        Product product = new Product("Product", null);
        productDAO.save(product);

        Product retrieved = productDAO.findById(product.getId());
        assertNull(retrieved.getDescription());
    }

    @Test
    @Order(11)
    void testUpdateProductWithNullDescription() throws SQLException {
        Product product = new Product("Product", "Initial");
        productDAO.save(product);

        product.setDescription(null);
        productDAO.update(product);

        Product updated = productDAO.findById(product.getId());
        assertNull(updated.getDescription());
    }

    @Test
    @Order(12)
    void testProductUniqueness() throws SQLException {
        Product p1 = new Product("P1", "D1");
        Product p2 = new Product("P2", "D2");

        productDAO.save(p1);
        productDAO.save(p2);

        assertNotEquals(p1.getId(), p2.getId());
    }
}