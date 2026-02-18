package com.tradestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class TradeStoreApplication {
    static {
        // Force UTC timezone early in the application startup
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.setProperty("user.timezone", "UTC");
    }
    
    public static void main(String[] args) {
        SpringApplication.run(TradeStoreApplication.class, args);
    }
}
