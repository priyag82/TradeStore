package com.tradestore.service;

import com.tradestore.entity.Trade;
import com.tradestore.exception.InvalidTradeException;
import com.tradestore.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@Transactional
@Validated
public class TradeServiceOptimized {

    private static final Logger logger = LoggerFactory.getLogger(TradeServiceOptimized.class);
    private static final int BATCH_SIZE = 1000;
    private static final Executor asyncExecutor = Executors.newFixedThreadPool(4);

    @Autowired
    private TradeRepository tradeRepository;

    /**
     * Process a single trade with validation
     */
    public Trade processTrade(@Valid Trade trade) {
        logger.info("Processing trade: {}", trade.getTradeId());

        validateMaturityDate(trade);
        validateVersion(trade);

        Trade savedTrade = tradeRepository.save(trade);
        logger.info("Successfully processed trade: {}", savedTrade.getTradeId());
        
        return savedTrade;
    }

    /**
     * Process multiple trades in bulk for better performance
     */
    @Transactional
    public List<Trade> processTradesInBulk(@Valid List<Trade> trades) {
        logger.info("Processing {} trades in bulk", trades.size());
        
        // Validate all trades first
        trades.forEach(this::validateMaturityDate);
        trades.forEach(this::validateVersion);
        
        // Save all trades in one batch
        List<Trade> savedTrades = tradeRepository.saveAll(trades);
        logger.info("Successfully processed {} trades in bulk", savedTrades.size());
        
        return savedTrades;
    }

    /**
     * Get trades with pagination for handling large datasets
     */
    public Page<Trade> getTradesPaginated(int page, int size, String sortBy, String sortDir) {
        logger.info("Getting trades with pagination - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                   page, size, sortBy, sortDir);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        return tradeRepository.findAll(pageable);
    }

    /**
     * Get trades by counter party with pagination
     */
    public Page<Trade> getTradesByCounterParty(String counterPartyId, int page, int size) {
        logger.info("Getting trades by counter party: {} - page: {}, size: {}", 
                   counterPartyId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return tradeRepository.findByCounterPartyId(counterPartyId, pageable);
    }

    /**
     * Optimized expiry marking with bulk operations
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void markExpiredTradesOptimized() {
        logger.info("Starting optimized scheduled task to mark expired trades");
        
        // Process in batches to avoid memory issues
        int page = 0;
        int totalExpired = 0;
        Pageable pageable = PageRequest.of(page, BATCH_SIZE);
        
        List<Trade> tradesToExpire;
        do {
            tradesToExpire = tradeRepository.findTradesToExpireBatch(LocalDate.now(), pageable);
            
            if (!tradesToExpire.isEmpty()) {
                // Mark all as expired in memory
                tradesToExpire.forEach(trade -> trade.setExpired(true));
                
                // Save all in one batch
                tradeRepository.saveAll(tradesToExpire);
                
                totalExpired += tradesToExpire.size();
                logger.info("Marked {} trades as expired (batch {})", tradesToExpire.size(), page + 1);
                
                page++;
                pageable = PageRequest.of(page, BATCH_SIZE);
            }
        } while (!tradesToExpire.isEmpty());
        
        logger.info("Completed marking {} trades as expired across {} batches", totalExpired, page);
    }

    /**
     * Async version of expiry marking for better performance
     */
    @Scheduled(cron = "0 30 0 * * *") // 30 minutes after midnight
    public void markExpiredTradesAsync() {
        logger.info("Starting async expiry task");
        
        CompletableFuture.runAsync(() -> {
            try {
                markExpiredTradesOptimized();
            } catch (Exception e) {
                logger.error("Error in async expiry task", e);
            }
        }, asyncExecutor);
    }

    private void validateMaturityDate(Trade trade) {
        if (trade.getMaturityDate().isBefore(LocalDate.now())) {
            String errorMessage = String.format("Trade maturity date %s cannot be before today", 
                                               trade.getMaturityDate());
            logger.error("Validation failed for trade {}: {}", trade.getTradeId(), errorMessage);
            throw new InvalidTradeException("Trade maturity date cannot be before today");
        }
    }

    private void validateVersion(Trade trade) {
        Optional<Trade> existingTradeOpt = tradeRepository.findById(trade.getTradeId());
        
        if (existingTradeOpt.isPresent()) {
            Trade existingTrade = existingTradeOpt.get();
            
            if (trade.getVersion() < existingTrade.getVersion()) {
                String errorMessage = String.format("Trade version %d is lower than existing version %d", 
                                                   trade.getVersion(), existingTrade.getVersion());
                logger.error("Version validation failed for trade {}: {}", trade.getTradeId(), errorMessage);
                throw new InvalidTradeException(errorMessage);
            }
            
            logger.info("Replacing existing trade {} with version {}", 
                       trade.getTradeId(), trade.getVersion());
        }
    }

    public Optional<Trade> getTrade(com.tradestore.domain.valueobject.TradeId tradeId) {
        return tradeRepository.findById(tradeId);
    }

    /**
     * Get trade count for monitoring
     */
    public long getTradeCount() {
        return tradeRepository.count();
    }

    /**
     * Get trade count by expiry status
     */
    public long getTradeCountByExpiry(boolean expired) {
        return tradeRepository.countByExpired(expired);
    }
}
