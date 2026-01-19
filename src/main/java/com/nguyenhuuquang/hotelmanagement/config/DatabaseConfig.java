package com.nguyenhuuquang.hotelmanagement.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        String username = System.getenv("PGUSER");
        String password = System.getenv("PGPASSWORD");

        if (databaseUrl != null && databaseUrl.contains("@")) {
            try {
                String urlWithoutProtocol = databaseUrl.substring(databaseUrl.indexOf("://") + 3);

                if (urlWithoutProtocol.contains("@")) {
                    String[] parts = urlWithoutProtocol.split("@");
                    String credentialsPart = parts[0];
                    String hostPart = parts[1];

                    if (username == null && credentialsPart.contains(":")) {
                        String[] credentials = credentialsPart.split(":", 2);
                        username = credentials[0];
                        password = credentials[1];
                    }

                    databaseUrl = "jdbc:postgresql://" + hostPart;
                }
            } catch (Exception e) {
                System.err.println("Error parsing DATABASE_URL: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            databaseUrl = "jdbc:" + databaseUrl;
        }

        if (databaseUrl == null) {
            databaseUrl = "jdbc:postgresql://localhost:5432/hotelmanagement";
            username = "postgres";
            password = "postgres";
        }

        System.out.println("=== Database Connection Info ===");
        System.out.println("URL: " + databaseUrl);
        System.out.println("Username: " + username);
        System.out.println("Password: " + (password != null ? "***" : "null"));
        System.out.println("================================");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(60000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.setInitializationFailTimeout(60000);

        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        config.setPoolName("HotelManagementHikariPool");

        config.setAutoCommit(true);

        return new HikariDataSource(config);
    }
}