package com.tradestore.controller;

import com.tradestore.entity.Trade;
import com.tradestore.exception.InvalidTradeException;
import com.tradestore.service.TradeServiceOptimized;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/trades")
@Validated
public class TradeControllerOptimized {

    private static final Logger logger = LoggerFactory.getLogger(TradeControllerOptimized.class);

    @Autowired
    private TradeServiceOptimized tradeService;

    /**
     * Get trades with pagination - optimized for large datasets
     */
    @GetMapping
    public ResponseEntity<Page<Trade>> getTradesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.info("Getting trades with pagination - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                   page, size, sortBy, sortDir);
        
        // Validate page size to prevent excessive memory usage
        if (size > 1000) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<Trade> trades = tradeService.getTradesPaginated(page, size, sortBy, sortDir);
        return ResponseEntity.ok(trades);
    }

    /**
     * Get trades by counter party with pagination
     */
    @GetMapping("/counter-party/{counterPartyId}")
    public ResponseEntity<Page<Trade>> getTradesByCounterParty(
            @PathVariable String counterPartyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        logger.info("Getting trades by counter party: {} - page: {}, size: {}", 
                   counterPartyId, page, size);
        
        if (size > 1000) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<Trade> trades = tradeService.getTradesByCounterParty(counterPartyId, page, size);
        return ResponseEntity.ok(trades);
    }

    /**
     * Get active (non-expired) trades
     */
    @GetMapping("/active")
    public ResponseEntity<Page<Trade>> getActiveTrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        logger.info("Getting active trades - page: {}, size: {}", page, size);
        
        if (size > 1000) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<Trade> trades = tradeService.getTradesPaginated(page, size, "createdDate", "desc");
        // Filter active trades (this would be better implemented in service layer)
        Page<Trade> activeTrades = trades.map(trade -> trade.isExpired() ? null : trade);
        return ResponseEntity.ok(activeTrades);
    }

    /**
     * Get a specific trade by ID
     */
    @GetMapping("/{tradeId}")
    public ResponseEntity<Trade> getTrade(@PathVariable String tradeId) {
        logger.info("Getting trade with ID: {}", tradeId);
        try {
            com.tradestore.domain.valueobject.TradeId id = com.tradestore.domain.valueobject.TradeId.from(tradeId);
            Optional<Trade> trade = tradeService.getTrade(id);
            
            return trade.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid trade ID format: {}", tradeId);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create a single trade
     */
    @PostMapping
    public ResponseEntity<Trade> createTrade(@Valid @RequestBody Trade trade) {
        logger.info("Creating new trade: {}", trade.getTradeId());
        
        try {
            Trade savedTrade = tradeService.processTrade(trade);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTrade);
        } catch (InvalidTradeException e) {
            logger.warn("Invalid trade data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating trade: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create multiple trades in bulk - optimized for performance
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<Trade>> createTradesInBulk(@Valid @RequestBody List<Trade> trades) {
        logger.info("Creating {} trades in bulk", trades.size());
        
        // Validate batch size to prevent memory issues
        if (trades.size() > 1000) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<Trade> savedTrades = tradeService.processTradesInBulk(trades);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTrades);
        } catch (InvalidTradeException e) {
            logger.warn("Invalid trade data in bulk: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating trades in bulk: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing trade
     */
    @PutMapping("/{tradeId}")
    public ResponseEntity<Trade> updateTrade(
            @PathVariable UUID tradeId, 
            @Valid @RequestBody Trade trade) {
        
        logger.info("Updating trade with ID: {}", tradeId);
        
        if (!tradeId.equals(trade.getTradeId())) {
            logger.error("Trade ID in path does not match trade ID in body");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Trade updatedTrade = tradeService.processTrade(trade);
            return ResponseEntity.ok(updatedTrade);
        } catch (InvalidTradeException e) {
            logger.warn("Invalid trade data for update: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating trade: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get trade statistics for monitoring
     */
    @GetMapping("/stats")
    public ResponseEntity<TradeStats> getTradeStats() {
        logger.info("Getting trade statistics");
        
        try {
            long totalTrades = tradeService.getTradeCount();
            long activeTrades = tradeService.getTradeCountByExpiry(false);
            long expiredTrades = tradeService.getTradeCountByExpiry(true);
            
            TradeStats stats = new TradeStats(totalTrades, activeTrades, expiredTrades);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting trade statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Trade service is healthy");
    }

    // DTO for statistics
    public static class TradeStats {
        private final long totalTrades;
        private final long activeTrades;
        private final long expiredTrades;

        public TradeStats(long totalTrades, long activeTrades, long expiredTrades) {
            this.totalTrades = totalTrades;
            this.activeTrades = activeTrades;
            this.expiredTrades = expiredTrades;
        }

        public long getTotalTrades() { return totalTrades; }
        public long getActiveTrades() { return activeTrades; }
        public long getExpiredTrades() { return expiredTrades; }
    }
}
