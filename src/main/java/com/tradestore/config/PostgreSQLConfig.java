package com.tradestore.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import javax.sql.DataSource;
import java.util.TimeZone;

@Configuration
public class PostgreSQLConfig {

    @Bean
    public DataSource dataSource(DataSourceProperties properties) {
        // Force UTC timezone for PostgreSQL connection
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        
        return DataSourceBuilder.create()
                .url(properties.determineUrl())
                .username(properties.determineUsername())
                .password(properties.determinePassword())
                .driverClassName(properties.determineDriverClassName())
                .build();
    }
}
