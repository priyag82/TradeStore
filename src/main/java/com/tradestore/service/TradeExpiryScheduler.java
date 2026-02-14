package com.tradestore.service;

import com.tradestore.entity.Trade;
import com.tradestore.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TradeExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TradeExpiryScheduler.class);

    @Autowired
    private TradeRepository tradeRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void expireTradesAtMidnight() {
        logger.info("Starting scheduled trade expiry check at midnight");
        int expiredCount = expireMaturedTrades();
        logger.info("Completed trade expiry check. Expired {} trades", expiredCount);
    }

    @Scheduled(fixedRate = 60000) // Every minute for demo
    public void expireTradesEveryMinute() {
        logger.debug("Running trade expiry check for testing purposes");
        int expiredCount = expireMaturedTrades();
        if (expiredCount > 0) {
            logger.info("Expired {} trades during scheduled check", expiredCount);
        }
    }

    @Transactional
    public int expireMaturedTrades() {
        LocalDate today = LocalDate.now();
        
        List<Trade> maturedTrades = tradeRepository.findTradesToExpireBatch(today, PageRequest.of(0, 1000));
        
        if (maturedTrades.isEmpty()) {
            logger.debug("No matured trades found for expiry");
            return 0;
        }

        logger.info("Found {} trades to expire with maturity date before today", maturedTrades.size());
        
        for (Trade trade : maturedTrades) {
            trade.setExpired(true);
            tradeRepository.save(trade);
            logger.debug("Expired trade: {} with maturity date: {}", 
                trade.getTradeId(), trade.getMaturityDate());
        }

        return maturedTrades.size();
    }
}
