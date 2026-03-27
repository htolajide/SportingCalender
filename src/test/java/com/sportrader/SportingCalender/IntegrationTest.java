package com.sportrader.SportingCalender;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class IntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                mysql::getJdbcUrl);

        registry.add(
                "spring.datasource.username",
                mysql::getUsername);

        registry.add(
                "spring.datasource.password",
                mysql::getPassword);

        registry.add(
                "spring.datasource.driver-class-name",
                mysql::getDriverClassName);

        registry.add(
                "spring.jpa.database-platform",
                () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Test
    void contextLoads() {
        assertTrue(mysql.isRunning());
    }

}