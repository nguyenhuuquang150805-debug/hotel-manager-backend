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
            databaseUrl = databaseUrl.replace("postgresql://", "jdbc:postgresql://");
        }

        if (databaseUrl == null) {
            databaseUrl = "jdbc:postgresql://localhost:5432/railway";
        }

        return DataSourceBuilder.create()
                .url(databaseUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}