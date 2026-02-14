package com.tradestore.performance;

import com.tradestore.entity.Trade;
import com.tradestore.service.TradeServiceOptimized;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class TradePerformanceTest {

    @Autowired
    private TradeServiceOptimized tradeService;

    private static final int LARGE_DATASET_SIZE = 10000;
    private static final int BATCH_SIZE = 1000;

    @Test
    void testBulkInsertPerformance() {
        // Create large dataset
        List<Trade> trades = generateTrades(LARGE_DATASET_SIZE);
        
        long startTime = System.currentTimeMillis();
        
        // Test bulk insert
        List<Trade> savedTrades = tradeService.processTradesInBulk(trades);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertEquals(LARGE_DATASET_SIZE, savedTrades.size());
        
        System.out.printf("Bulk insert of %d trades took %d ms (%.2f trades/sec)%n", 
                         LARGE_DATASET_SIZE, duration, (double) LARGE_DATASET_SIZE / duration * 1000);
        
        // Performance assertion - should complete within reasonable time
        assertTrue(duration < 10000, "Bulk insert should complete within 10 seconds");
    }

    @Test
    void testPaginationPerformance() {
        // Insert test data
        List<Trade> trades = generateTrades(5000);
        tradeService.processTradesInBulk(trades);
        
        long startTime = System.currentTimeMillis();
        
        // Test pagination performance
        int totalPages = 0;
        int page = 0;
        Page<Trade> tradePage;
        
        do {
            tradePage = tradeService.getTradesPaginated(page, 100, "createdDate", "desc");
            totalPages++;
            page++;
            
            // Simulate processing
            assertNotNull(tradePage.getContent());
            
        } while (tradePage.hasNext() && page < 100); // Safety limit
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.printf("Pagination of %d trades took %d ms (%d pages)%n", 
                         5000, duration, totalPages);
        
        assertTrue(duration < 5000, "Pagination should complete within 5 seconds");
    }

    @Test
    void testConcurrentAccessPerformance() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // Simulate concurrent access
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<Trade> trades = generateTrades(100);
                
                // Add thread identifier to trade IDs
                trades.forEach(trade -> {
                    trade.setTradeId(UUID.randomUUID());
                    trade.setCounterPartyId("COUNTER_" + threadId);
                });
                
                tradeService.processTradesInBulk(trades);
            }, executor);
            
            futures.add(future);
        }
        
        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.printf("Concurrent insert of 1000 trades (10 threads) took %d ms%n", duration);
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        assertTrue(duration < 15000, "Concurrent operations should complete within 15 seconds");
    }

    @Test
    void testExpiryPerformance() {
        // Create trades that will expire
        List<Trade> trades = new ArrayList<>();
        LocalDate pastDate = LocalDate.now().minusDays(1);
        
        for (int i = 0; i < 5000; i++) {
            Trade trade = new Trade(
                UUID.randomUUID(),
                1,
                "COUNTER_PARTY_" + i,
                "BOOK_" + (i % 100),
                pastDate, // Will expire
                LocalDate.now(),
                false
            );
            trades.add(trade);
        }
        
        // Insert trades
        tradeService.processTradesInBulk(trades);
        
        long startTime = System.currentTimeMillis();
        
        // Test expiry performance
        tradeService.markExpiredTradesOptimized();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.printf("Expiry processing of 5000 trades took %d ms%n", duration);
        
        assertTrue(duration < 5000, "Expiry processing should complete within 5 seconds");
    }

    @Test
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        
        // Force garbage collection
        System.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Create and process large dataset
        List<Trade> trades = generateTrades(LARGE_DATASET_SIZE);
        tradeService.processTradesInBulk(trades);
        
        // Test pagination to ensure memory doesn't grow excessively
        for (int i = 0; i < 10; i++) {
            Page<Trade> page = tradeService.getTradesPaginated(i, 100, "createdDate", "desc");
            assertNotNull(page.getContent());
        }
        
        System.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        System.out.printf("Memory usage for %d trades: %.2f MB%n", 
                         LARGE_DATASET_SIZE, memoryUsed / (1024.0 * 1024.0));
        
        // Memory usage should be reasonable (less than 100MB for 10k trades)
        assertTrue(memoryUsed < 100 * 1024 * 1024, "Memory usage should be under 100MB");
    }

    private List<Trade> generateTrades(int count) {
        List<Trade> trades = new ArrayList<>();
        LocalDate futureDate = LocalDate.now().plusYears(1);
        
        for (int i = 0; i < count; i++) {
            Trade trade = new Trade(
                UUID.randomUUID(),
                1,
                "COUNTER_PARTY_" + (i % 1000), // Distribute across 1000 counter parties
                "BOOK_" + (i % 100), // Distribute across 100 books
                futureDate,
                LocalDate.now(),
                false
            );
            trades.add(trade);
        }
        
        return trades;
    }

    @Test
    void testSearchPerformance() {
        // Insert test data
        List<Trade> trades = generateTrades(5000);
        tradeService.processTradesInBulk(trades);
        
        long startTime = System.currentTimeMillis();
        
        // Test search by counter party
        Page<Trade> searchResults = tradeService.getTradesByCounterParty("COUNTER_PARTY_1", 0, 50);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.printf("Search by counter party took %d ms (found %d results)%n", 
                         duration, searchResults.getTotalElements());
        
        assertTrue(duration < 1000, "Search should complete within 1 second");
        assertTrue(searchResults.getTotalElements() > 0, "Should find some results");
    }
}
