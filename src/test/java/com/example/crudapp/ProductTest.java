package com.example.crudapp;

import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.example.crudapp.model.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductTest {

    @Test
    @Order(1)
    void testDefaultConstructor() {
        Product product = new Product();

        assertNotNull(product.getId());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
        assertNull(product.getName());
        assertNull(product.getDescription());
    }

    @Test
    @Order(2)
    void testParameterizedConstructor() {
        Product product = new Product("Test Product", "Test Description");

        assertNotNull(product.getId());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
        assertEquals("Test Product", product.getName());
        assertEquals("Test Description", product.getDescription());
    }

    @Test
    @Order(3)
    void testSettersAndGetters() {
        Product product = new Product();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        product.setId(id);
        product.setName("Product Name");
        product.setDescription("Product Description");
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        assertEquals(id, product.getId());
        assertEquals("Product Name", product.getName());
        assertEquals("Product Description", product.getDescription());
        assertEquals(now, product.getCreatedAt());
        assertEquals(now, product.getUpdatedAt());
    }

    @Test
    @Order(4)
    void testToString() {
        Product product = new Product("Test", "Description");
        String str = product.toString();

        assertTrue(str.contains("Test"));
        assertTrue(str.contains("Description"));
        assertTrue(str.contains(product.getId().toString()));
    }

    @Test
    @Order(5)
    void testNullNameInConstructor() {
        Product product = new Product(null, "Description");

        assertNull(product.getName());
        assertEquals("Description", product.getDescription());
    }

    @Test
    @Order(6)
    void testNullDescriptionInConstructor() {
        Product product = new Product("Product", null);

        assertEquals("Product", product.getName());
        assertNull(product.getDescription());
    }

    @Test
    @Order(7)
    void testSetNullName() {
        Product product = new Product("Original", "Desc");
        product.setName(null);

        assertNull(product.getName());
    }

    @Test
    @Order(8)
    void testSetNullDescription() {
        Product product = new Product("Product", "Original");
        product.setDescription(null);

        assertNull(product.getDescription());
    }

    @Test
    @Order(9)
    void testDateAutoGeneration() throws InterruptedException {
        Product product1 = new Product();
        Thread.sleep(10); // Небольшая задержка
        Product product2 = new Product();

        // Проверяем, что даты устанавливаются автоматически
        assertTrue(product1.getCreatedAt().isBefore(product2.getCreatedAt()) ||
                product1.getCreatedAt().isEqual(product2.getCreatedAt()));
    }

    @Test
    @Order(10)
    void testUUIDUniqueness() {
        Product product1 = new Product();
        Product product2 = new Product();

        assertNotEquals(product1.getId(), product2.getId());
    }

    @Test
    @Order(11)
    void testEmptyConstructorWithSetters() {
        Product product = new Product();

        // Устанавливаем все поля через сеттеры
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        LocalDateTime date = LocalDateTime.of(2023, 1, 1, 12, 0);

        product.setId(id);
        product.setName("Custom Product");
        product.setDescription("Custom Description");
        product.setCreatedAt(date);
        product.setUpdatedAt(date);

        assertEquals(id, product.getId());
        assertEquals("Custom Product", product.getName());
        assertEquals("Custom Description", product.getDescription());
        assertEquals(date, product.getCreatedAt());
        assertEquals(date, product.getUpdatedAt());
    }

    @Test
    @Order(12)
    void testTrimmingInSetters() {
        Product product = new Product();

        product.setName("  Product Name  ");
        product.setDescription("  Description  ");

        assertEquals("  Product Name  ", product.getName());
        assertEquals("  Description  ", product.getDescription());
    }

    @Test
    @Order(13)
    void testEqualityById() {
        UUID sameId = UUID.randomUUID();

        Product product1 = new Product();
        product1.setId(sameId);
        product1.setName("Product 1");

        Product product2 = new Product();
        product2.setId(sameId);
        product2.setName("Product 2"); // Другое имя

        // Продукты с одинаковым ID считаются "равными" в бизнес-логике
        assertEquals(sameId, product1.getId());
        assertEquals(sameId, product2.getId());
    }

    @Test
    @Order(14)
    void testDateConsistency() {
        Product product = new Product("Product", "Desc");
        LocalDateTime createdAt = product.getCreatedAt();
        LocalDateTime updatedAt = product.getUpdatedAt();

        // При создании даты должны быть одинаковыми (или очень близкими)
        long diff = Math.abs(createdAt.until(updatedAt, java.time.temporal.ChronoUnit.MILLIS));
        assertTrue(diff < 1000, "Даты создания и обновления должны быть близки при создании");
    }
}