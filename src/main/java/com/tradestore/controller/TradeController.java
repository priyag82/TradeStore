package com.tradestore.controller;

import com.tradestore.domain.valueobject.TradeId;
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
    public ResponseEntity<Trade> getTrade(@PathVariable String tradeId) {
        logger.info("Getting trade with ID: {}", tradeId);
        try {
            TradeId id = TradeId.from(tradeId);
            Optional<Trade> trade = tradeService.getTrade(id);
            
            if (trade.isPresent()) {
                return ResponseEntity.ok(trade.get());
            } else {
                logger.warn("Trade not found with ID: {}", tradeId);
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid trade ID format: {}", tradeId);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<Trade> createTrade(@RequestBody(required = false) Trade trade) {
        logger.info("Creating new trade: {}", trade != null ? trade.getTradeId() : "null");
        
        if (trade == null) {
            logger.error("Empty request body received");
            return ResponseEntity.badRequest().body("Request body cannot be empty");
        }
        
        try {
            Trade savedTrade = tradeService.processTrade(trade);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTrade);
        } catch (Exception e) {
            logger.error("Error creating trade: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{tradeId}")
    public ResponseEntity<Trade> updateTrade(@PathVariable String tradeId, @RequestBody Trade trade) {
        logger.info("Updating trade with ID: {}", tradeId);
        
        try {
            TradeId pathId = TradeId.from(tradeId);
            if (!pathId.equals(trade.getTradeId())) {
                logger.error("Trade ID in path does not match trade ID in body");
                return ResponseEntity.badRequest().build();
            }
            
            Trade updatedTrade = tradeService.processTrade(trade);
            return ResponseEntity.ok(updatedTrade);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid trade ID format: {}", tradeId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating trade: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{tradeId}")
    public ResponseEntity<Void> deleteTrade(@PathVariable String tradeId) {
        logger.info("Deleting trade with ID: {}", tradeId);
        try {
            TradeId.from(tradeId); // Validate format
            // Note: This would require implementing delete in TradeService and TradeRepository
            // For now, returning not implemented
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid trade ID format: {}", tradeId);
            return ResponseEntity.badRequest().build();
        }
    }
}
