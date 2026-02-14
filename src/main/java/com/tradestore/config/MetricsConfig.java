package com.tradestore.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    @Bean
    public Counter rejectedTradesCounter() {
        return Counter.builder("trades.rejected.total")
                .description("Total number of rejected trades")
                .tag("reason", "version_conflict")
                .register(meterRegistry);
    }

    @Bean
    public Counter processedTradesCounter() {
        return Counter.builder("trades.processed.total")
                .description("Total number of processed trades")
                .register(meterRegistry);
    }

    @Bean
    public Counter expiredTradesCounter() {
        return Counter.builder("trades.expired.total")
                .description("Total number of expired trades")
                .register(meterRegistry);
    }
}
