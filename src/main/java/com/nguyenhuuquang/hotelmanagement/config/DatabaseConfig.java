package com.nguyenhuuquang.hotelmanagement.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        String username = System.getenv("PGUSER");
        String password = System.getenv("PGPASSWORD");

        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
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
        System.out.println("================================");

        return DataSourceBuilder.create()
                .url(databaseUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}