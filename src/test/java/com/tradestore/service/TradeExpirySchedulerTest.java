package com.tradestore.service;

import com.tradestore.entity.Trade;
import com.tradestore.repository.TradeRepository;
import com.tradestore.domain.valueobject.TradeId;
import com.tradestore.domain.valueobject.CounterPartyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.jakarta.persistence.validation.mode=none"
})
class TradeExpirySchedulerTest {

    @Autowired
    private TradeExpiryScheduler tradeExpiryScheduler;

    @Autowired
    private TradeRepository tradeRepository;

    @BeforeEach
    void setUp() {
        tradeRepository.deleteAll();
    }

    @Test
    void testExpireMaturedTrades() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        Trade expiredTrade = createTrade(TradeId.generate(), 1, CounterPartyId.from("COUNTER_1"), "BOOK_1", yesterday, false);
        Trade validTrade = createTrade(TradeId.generate(), 1, CounterPartyId.from("COUNTER_2"), "BOOK_2", tomorrow, false);
        Trade alreadyExpiredTrade = createTrade(TradeId.generate(), 1, CounterPartyId.from("COUNTER_3"), "BOOK_3", yesterday, true);

        tradeRepository.save(expiredTrade);
        tradeRepository.save(validTrade);
        tradeRepository.save(alreadyExpiredTrade);

        int expiredCount = tradeExpiryScheduler.expireMaturedTrades();

        assertEquals(1, expiredCount);

        Trade updatedExpiredTrade = tradeRepository.findById(expiredTrade.getTradeId()).orElse(null);
        assertNotNull(updatedExpiredTrade);
        assertTrue(updatedExpiredTrade.isExpired());

        Trade unchangedValidTrade = tradeRepository.findById(validTrade.getTradeId()).orElse(null);
        assertNotNull(unchangedValidTrade);
        assertFalse(unchangedValidTrade.isExpired());

        Trade unchangedAlreadyExpiredTrade = tradeRepository.findById(alreadyExpiredTrade.getTradeId()).orElse(null);
        assertNotNull(unchangedAlreadyExpiredTrade);
        assertTrue(unchangedAlreadyExpiredTrade.isExpired());
    }

    @Test
    void testExpireMaturedTrades_NoTradesToExpire() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        Trade validTrade1 = createTrade(TradeId.generate(), 1, CounterPartyId.from("COUNTER_1"), "BOOK_1", tomorrow, false);
        Trade validTrade2 = createTrade(TradeId.generate(), 1, CounterPartyId.from("COUNTER_2"), "BOOK_2", tomorrow, false);

        tradeRepository.save(validTrade1);
        tradeRepository.save(validTrade2);

        int expiredCount = tradeExpiryScheduler.expireMaturedTrades();

        assertEquals(0, expiredCount);

        List<Trade> allTrades = tradeRepository.findAll();
        assertEquals(2, allTrades.size());
        allTrades.forEach(trade -> assertFalse(trade.isExpired()));
    }

    @Test
    void testExpireMaturedTrades_EmptyDatabase() {
        int expiredCount = tradeExpiryScheduler.expireMaturedTrades();
        assertEquals(0, expiredCount);
    }

    @Test
    void testExpireMaturedTrades_MultipleExpiredTrades() {
        LocalDate today = LocalDate.now();
        LocalDate lastWeek = today.minusWeeks(1);
        LocalDate yesterday = today.minusDays(1);

        Trade expiredTrade1 = createTrade(TradeId.generate(), 1, CounterPartyId.from("COUNTER_1"), "BOOK_1", lastWeek, false);
        Trade expiredTrade2 = createTrade(TradeId.generate(), 1, CounterPartyId.from("COUNTER_2"), "BOOK_2", yesterday, false);
        Trade validTrade = createTrade(TradeId.generate(), 1, CounterPartyId.from("COUNTER_3"), "BOOK_3", today.plusDays(1), false);

        tradeRepository.save(expiredTrade1);
        tradeRepository.save(expiredTrade2);
        tradeRepository.save(validTrade);

        int expiredCount = tradeExpiryScheduler.expireMaturedTrades();

        assertEquals(2, expiredCount);

        Trade updatedExpiredTrade1 = tradeRepository.findById(expiredTrade1.getTradeId()).orElse(null);
        assertNotNull(updatedExpiredTrade1);
        assertTrue(updatedExpiredTrade1.isExpired());

        Trade updatedExpiredTrade2 = tradeRepository.findById(expiredTrade2.getTradeId()).orElse(null);
        assertNotNull(updatedExpiredTrade2);
        assertTrue(updatedExpiredTrade2.isExpired());

        Trade unchangedValidTrade = tradeRepository.findById(validTrade.getTradeId()).orElse(null);
        assertNotNull(unchangedValidTrade);
        assertFalse(unchangedValidTrade.isExpired());
    }

    private Trade createTrade(TradeId tradeId, int version, CounterPartyId counterPartyId, String bookId, 
                           LocalDate maturityDate, boolean expired) {
        Trade trade = new Trade();
        trade.setTradeId(tradeId);
        trade.setVersion(version);
        trade.setCounterPartyId(counterPartyId);
        trade.setBookId(bookId);
        trade.setCreatedDate(LocalDate.now());
        trade.setExpired(expired);
        // Use reflection to set maturity date without validation for testing
        try {
            java.lang.reflect.Field field = Trade.class.getDeclaredField("maturityDate");
            field.setAccessible(true);
            field.set(trade, maturityDate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return trade;
    }
}
