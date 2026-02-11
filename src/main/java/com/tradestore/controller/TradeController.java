package com.tradestore.controller;

import com.tradestore.entity.Trade;
import com.tradestore.service.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

    @Autowired
    private TradeService tradeService;

    @GetMapping
    public ResponseEntity<List<Trade>> getAllTrades() {
        logger.info("Getting all trades");
        List<Trade> trades = tradeService.getAllTrades();
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/{tradeId}")
    public ResponseEntity<Trade> getTrade(@PathVariable UUID tradeId) {
        logger.info("Getting trade with ID: {}", tradeId);
        Optional<Trade> trade = tradeService.getTrade(tradeId);
        
        if (trade.isPresent()) {
            return ResponseEntity.ok(trade.get());
        } else {
            logger.warn("Trade not found with ID: {}", tradeId);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Trade> createTrade(@RequestBody Trade trade) {
        logger.info("Creating new trade: {}", trade.getTradeId());
        try {
            Trade savedTrade = tradeService.processTrade(trade);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTrade);
        } catch (Exception e) {
            logger.error("Error creating trade: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{tradeId}")
    public ResponseEntity<Trade> updateTrade(@PathVariable UUID tradeId, @RequestBody Trade trade) {
        logger.info("Updating trade with ID: {}", tradeId);
        
        if (!tradeId.equals(trade.getTradeId())) {
            logger.error("Trade ID in path does not match trade ID in body");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Trade updatedTrade = tradeService.processTrade(trade);
            return ResponseEntity.ok(updatedTrade);
        } catch (Exception e) {
            logger.error("Error updating trade: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{tradeId}")
    public ResponseEntity<Void> deleteTrade(@PathVariable UUID tradeId) {
        logger.info("Deleting trade with ID: {}", tradeId);
        // Note: This would require implementing delete in TradeService and TradeRepository
        // For now, returning not implemented
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
