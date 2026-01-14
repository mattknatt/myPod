package org.example;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class LoggerManager {

    private static final Logger logger = LoggerFactory.getLogger(LoggerManager.class);

    // JDBC connection details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "myPodLogs";
    private static final String USER = "user";
    private static final String PASSWORD = "password";

    public static void setup() {
        try {
            // Step 1: Connect to MySQL server (no database selected yet)
            try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {

                // Create database if it does not exist
                String createDbSQL = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
                stmt.executeUpdate(createDbSQL);
                logger.info("Database checked/created successfully.");
            }

            // Step 2: Connect to the specific database
            try (Connection conn = DriverManager.getConnection(JDBC_URL + DB_NAME, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {

                // Create table if it does not exist
                String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS logs (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    log_date_time TIMESTAMP NULL,
                    `level` VARCHAR(255) NULL,
                    clazz VARCHAR(255) NULL,
                    log TEXT NULL,
                    `exception` TEXT NULL,
                    job_run_id VARCHAR(255) NULL,
                    created_by VARCHAR(50)
                    NOT NULL DEFAULT 'system',
                    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    last_modified_by VARCHAR(50) NULL,
                    last_modified_date TIMESTAMP NULL,
                    PRIMARY KEY (id)
                    )
                    """;
                stmt.executeUpdate(createTableSQL);
                System.out.println("Table checked/created successfully.");
            }

        } catch (SQLException e) {
            logger.error("Database error: {}", e.getMessage());

            throw new RuntimeException("Failed to initialize logging database", e);
        }
    }
}

