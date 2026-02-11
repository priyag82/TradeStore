package com.tradestore.service;

import com.tradestore.entity.Trade;
import com.tradestore.exception.InvalidTradeException;
import com.tradestore.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Trade processTrade(Trade trade) {
        logger.info("Processing trade: {}", trade.getTradeId());

        validateMaturityDate(trade);
        validateVersion(trade);

        Trade savedTrade = tradeRepository.save(trade);
        logger.info("Successfully processed trade: {}", savedTrade.getTradeId());
        
        return savedTrade;
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

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void markExpiredTrades() {
        logger.info("Starting scheduled task to mark expired trades");
        
        List<Trade> tradesToExpire = tradeRepository.findByMaturityDateBeforeAndExpired(
            LocalDate.now(), "N");
        
        if (tradesToExpire.isEmpty()) {
            logger.info("No trades to expire");
            return;
        }
        
        logger.info("Found {} trades to expire", tradesToExpire.size());
        
        for (Trade trade : tradesToExpire) {
            trade.setExpired("Y");
            tradeRepository.save(trade);
            logger.debug("Marked trade {} as expired", trade.getTradeId());
        }
        
        logger.info("Completed marking {} trades as expired", tradesToExpire.size());
    }

    public Optional<Trade> getTrade(UUID tradeId) {
        return tradeRepository.findById(tradeId);
    }

    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }
}
