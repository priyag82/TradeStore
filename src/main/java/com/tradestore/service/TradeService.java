package com.tradestore.service;

import com.tradestore.domain.valueobject.TradeId;
import com.tradestore.entity.Trade;
import com.tradestore.exception.InvalidTradeException;
import com.tradestore.exception.VersionConflictException;
import com.tradestore.repository.TradeRepository;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private TradeRepository tradeRepository;
    
    @Autowired
    private Counter rejectedTradesCounter;
    
    @Autowired
    private Counter processedTradesCounter;
    
    @Autowired
    private Counter expiredTradesCounter;

    public Trade processTrade(Trade trade) {
        logger.info("Processing trade: {}", trade.getTradeId());

        try {
            validateVersion(trade);
            validateMaturityDate(trade);
            Trade savedTrade = tradeRepository.save(trade);
            processedTradesCounter.increment();
            logger.info("Successfully processed trade: {}", savedTrade.getTradeId());
            return savedTrade;
        } catch (OptimisticLockingFailureException e) {
            rejectedTradesCounter.increment();
            logger.error("Optimistic locking failed for trade {}: {}", trade.getTradeId(), e.getMessage());
            throw new VersionConflictException("Trade was modified by another transaction. Please retry.", e);
        } catch (VersionConflictException e) {
            rejectedTradesCounter.increment();
            logger.error("Version conflict for trade {}: {}", trade.getTradeId(), e.getMessage());
            throw e;
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
                throw new VersionConflictException(errorMessage);
            }
            
            logger.info("Replacing existing trade {} with version {}", 
                       trade.getTradeId(), trade.getVersion());
        }
    }

    private void validateMaturityDate(Trade trade) {
        if (trade.getMaturityDate() != null && trade.getMaturityDate().isBefore(LocalDate.now())) {
            String errorMessage = "Trade maturity date cannot be before today";
            logger.error("Maturity date validation failed for trade {}: {}", trade.getTradeId(), errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void markExpiredTrades() {
        logger.info("Starting scheduled task to mark expired trades");
        
        List<Trade> tradesToExpire = tradeRepository.findTradesToExpireBatch(
            LocalDate.now(), PageRequest.of(0, 1000));
        
        if (tradesToExpire.isEmpty()) {
            logger.info("No trades to expire");
            return;
        }
        
        logger.info("Found {} trades to expire", tradesToExpire.size());
        
        for (Trade trade : tradesToExpire) {
            trade.setExpired(true);
            tradeRepository.save(trade);
            expiredTradesCounter.increment();
            logger.debug("Marked trade {} as expired", trade.getTradeId());
        }
        
        logger.info("Completed marking {} trades as expired", tradesToExpire.size());
    }

    public Optional<Trade> getTrade(TradeId tradeId) {
        return tradeRepository.findById(tradeId);
    }

    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }
}
