package com.example.crudapp;

import com.example.crudapp.dao.ProductDAO;
import com.example.crudapp.model.Product;
import com.example.crudapp.service.ProductService;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductServiceTest {

    private ProductService productService;
    private MockProductDAO mockDAO;

    private static class MockProductDAO extends ProductDAO {
        private final List<Product> products = new ArrayList<>();
        private boolean throwException = false;

        void setThrowException(boolean value) {
            throwException = value;
        }

        @Override
        public void save(Product product) throws SQLException {
            if (throwException) throw new SQLException("Test exception");
            products.add(product);
        }

        @Override
        public List<Product> findAll() throws SQLException {
            if (throwException) throw new SQLException("Test exception");
            return new ArrayList<>(products);
        }

        @Override
        public Product findById(UUID id) throws SQLException {
            if (throwException) throw new SQLException("Test exception");
            return products.stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void update(Product product) throws SQLException {
            if (throwException) throw new SQLException("Test exception");
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId().equals(product.getId())) {
                    products.set(i, product);
                    return;
                }
            }
        }

        @Override
        public void delete(UUID id) throws SQLException {
            if (throwException) throw new SQLException("Test exception");
            products.removeIf(p -> p.getId().equals(id));
        }

        @Override
        public List<Product> findByName(String name) throws SQLException {
            if (throwException) throw new SQLException("Test exception");
            List<Product> result = new ArrayList<>();
            for (Product product : products) {
                if (product.getName().toLowerCase().contains(name.toLowerCase())) {
                    result.add(product);
                }
            }
            return result;
        }

        @Override
        public int countAll() throws SQLException {
            if (throwException) throw new SQLException("Test exception");
            return products.size();
        }

        @Override
        public List<Product> findWithPagination(int offset, int limit) throws SQLException {
            if (throwException) throw new SQLException("Test exception");
            List<Product> result = new ArrayList<>();
            int start = Math.max(0, Math.min(offset, products.size()));
            int end = Math.min(start + limit, products.size());

            for (int i = start; i < end; i++) {
                result.add(products.get(i));
            }
            return result;
        }

        @Override
        protected Connection getConnection() {
            return null;
        }
    }

    @BeforeEach
    void setUp() {
        mockDAO = new MockProductDAO();
        productService = new ProductService() {
            @Override
            protected ProductDAO createProductDAO() {
                return mockDAO;
            }
        };
    }

    @Test
    @Order(1)
    void testCreateProductWithValidData() throws SQLException {
        productService.createProduct("Valid Product", "Valid Description");

        List<Product> products = productService.getAllProducts();
        assertEquals(1, products.size());
        assertEquals("Valid Product", products.get(0).getName());
    }

    @Test
    @Order(2)
    void testCreateProductWithInvalidNameShort() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct("AB", "Description"));
        assertTrue(exception.getMessage().contains("3 до 50"));
    }

    @Test
    @Order(3)
    void testCreateProductWithInvalidNameLong() {
        String longName = "A".repeat(51);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(longName, "Description"));
        assertTrue(exception.getMessage().contains("3 до 50"));
    }

    @Test
    @Order(4)
    void testCreateProductWithTooLongDescription() {
        String longDesc = "D".repeat(256);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct("Valid Name", longDesc));
        assertTrue(exception.getMessage().contains("255"));
    }

    @Test
    @Order(5)
    void testCreateProductWithNullName() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(null, "Description"));
        assertTrue(exception.getMessage().contains("3 до 50"));
    }

    @Test
    @Order(6)
    void testCreateProductWithEmptyName() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct("   ", "Description"));
        assertTrue(exception.getMessage().contains("3 до 50"));
    }

    @Test
    @Order(7)
    void testCreateProductTrimsInput() throws SQLException {
        productService.createProduct("  Product Name  ", "  Description  ");

        List<Product> products = productService.getAllProducts();
        assertEquals("Product Name", products.get(0).getName());
        assertEquals("Description", products.get(0).getDescription());
    }

    @Test
    @Order(8)
    void testCreateProductWithNullDescription() throws SQLException {
        productService.createProduct("Product", null);

        List<Product> products = productService.getAllProducts();
        assertNull(products.get(0).getDescription());
    }

    @Test
    @Order(9)
    void testGetAllProducts() throws SQLException {
        productService.createProduct("Product 1", "Desc 1");
        productService.createProduct("Product 2", "Desc 2");

        List<Product> products = productService.getAllProducts();
        assertEquals(2, products.size());
    }

    @Test
    @Order(10)
    void testGetAllProductsEmpty() throws SQLException {
        List<Product> products = productService.getAllProducts();
        assertTrue(products.isEmpty());
    }

    @Test
    @Order(11)
    void testGetProductsWithPagination() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            productService.createProduct("Product " + i, "Desc " + i);
        }

        List<Product> page1 = productService.getProductsWithPagination(1, 5);
        assertEquals(5, page1.size());

        List<Product> page2 = productService.getProductsWithPagination(2, 5);
        assertEquals(5, page2.size());
    }

    @Test
    @Order(12)
    void testSearchProductsByName() throws SQLException {
        productService.createProduct("Apple iPhone", "Smartphone");
        productService.createProduct("Apple MacBook", "Laptop");
        productService.createProduct("Samsung Galaxy", "Phone");

        List<Product> appleProducts = productService.searchProductsByName("apple");
        assertEquals(2, appleProducts.size());

        List<Product> samsungProducts = productService.searchProductsByName("samsung");
        assertEquals(1, samsungProducts.size());
    }

    @Test
    @Order(13)
    void testGetProductById() throws SQLException {
        productService.createProduct("Test Product", "Test Description");
        List<Product> products = productService.getAllProducts();
        UUID productId = products.get(0).getId();

        Product found = productService.getProductById(productId);
        assertNotNull(found);
        assertEquals(productId, found.getId());
    }

    @Test
    @Order(14)
    void testGetProductByIdNotFound() throws SQLException {
        Product found = productService.getProductById(UUID.randomUUID());
        assertNull(found);
    }

    @Test
    @Order(15)
    void testUpdateProduct() throws SQLException {
        productService.createProduct("Old Name", "Old Description");
        List<Product> products = productService.getAllProducts();
        Product product = products.get(0);

        product.setName("New Name");
        product.setDescription("New Description");
        productService.updateProduct(product);

        Product updated = productService.getProductById(product.getId());
        assertEquals("New Name", updated.getName());
    }

    @Test
    @Order(16)
    void testUpdateProductWithInvalidData() throws SQLException {
        productService.createProduct("Valid", "Desc");
        List<Product> products = productService.getAllProducts();
        Product product = products.get(0);

        product.setName("AB"); // Слишком короткое

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> productService.updateProduct(product));
        assertTrue(exception.getMessage().contains("3 до 50"));
    }

    @Test
    @Order(17)
    void testDeleteProduct() throws SQLException {
        productService.createProduct("To Delete", "Description");
        List<Product> products = productService.getAllProducts();
        UUID productId = products.get(0).getId();

        productService.deleteProduct(productId);

        Product deleted = productService.getProductById(productId);
        assertNull(deleted);
    }

    @Test
    @Order(18)
    void testGetTotalProductCount() throws SQLException {
        assertEquals(0, productService.getTotalProductCount());

        productService.createProduct("Product 1", "Desc 1");
        assertEquals(1, productService.getTotalProductCount());

        productService.createProduct("Product 2", "Desc 2");
        assertEquals(2, productService.getTotalProductCount());
    }

    @Test
    @Order(19)
    void testGetTotalPages() throws SQLException {
        for (int i = 1; i <= 15; i++) {
            productService.createProduct("Product " + i, "Desc " + i);
        }

        assertEquals(2, productService.getTotalPages(10)); // 15/10 = 2 страницы
        assertEquals(1, productService.getTotalPages(20)); // 15/20 = 1 страница
        assertEquals(5, productService.getTotalPages(3));  // 15/3 = 5 страниц
    }

    @Test
    @Order(20)
    void testGetTotalPagesWithZeroProducts() throws SQLException {
        assertEquals(0, productService.getTotalPages(10)); // 0 продуктов = 0 страниц
    }

    @Test
    @Order(21)
    void testDatabaseExceptionHandling() throws SQLException {
        mockDAO.setThrowException(true);

        assertThrows(SQLException.class,
                () -> productService.getAllProducts());

        assertThrows(SQLException.class,
                () -> productService.createProduct("Test", "Desc"));

        assertThrows(SQLException.class,
                () -> productService.getTotalProductCount());
    }
}
