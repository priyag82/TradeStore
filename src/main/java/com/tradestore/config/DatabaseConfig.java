package com.tradestore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.tradestore.repository")
@EnableMongoRepositories(basePackages = "com.tradestore.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
}
