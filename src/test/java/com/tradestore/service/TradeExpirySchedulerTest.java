package com.tradestore.service;

import com.tradestore.entity.Trade;
import com.tradestore.repository.TradeRepository;
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
    "spring.jpa.hibernate.ddl-auto=create-drop"
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

        Trade expiredTrade = createTrade(UUID.randomUUID(), 1, "COUNTER_1", "BOOK_1", yesterday, "N");
        Trade validTrade = createTrade(UUID.randomUUID(), 1, "COUNTER_2", "BOOK_2", tomorrow, "N");
        Trade alreadyExpiredTrade = createTrade(UUID.randomUUID(), 1, "COUNTER_3", "BOOK_3", yesterday, "Y");

        tradeRepository.save(expiredTrade);
        tradeRepository.save(validTrade);
        tradeRepository.save(alreadyExpiredTrade);

        int expiredCount = tradeExpiryScheduler.expireMaturedTrades();

        assertEquals(1, expiredCount);

        Trade updatedExpiredTrade = tradeRepository.findById(expiredTrade.getTradeId()).orElse(null);
        assertNotNull(updatedExpiredTrade);
        assertEquals("Y", updatedExpiredTrade.getExpired());

        Trade unchangedValidTrade = tradeRepository.findById(validTrade.getTradeId()).orElse(null);
        assertNotNull(unchangedValidTrade);
        assertEquals("N", unchangedValidTrade.getExpired());

        Trade unchangedAlreadyExpiredTrade = tradeRepository.findById(alreadyExpiredTrade.getTradeId()).orElse(null);
        assertNotNull(unchangedAlreadyExpiredTrade);
        assertEquals("Y", unchangedAlreadyExpiredTrade.getExpired());
    }

    @Test
    void testExpireMaturedTrades_NoTradesToExpire() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        Trade validTrade1 = createTrade(UUID.randomUUID(), 1, "COUNTER_1", "BOOK_1", tomorrow, "N");
        Trade validTrade2 = createTrade(UUID.randomUUID(), 1, "COUNTER_2", "BOOK_2", tomorrow, "N");

        tradeRepository.save(validTrade1);
        tradeRepository.save(validTrade2);

        int expiredCount = tradeExpiryScheduler.expireMaturedTrades();

        assertEquals(0, expiredCount);

        List<Trade> allTrades = tradeRepository.findAll();
        assertEquals(2, allTrades.size());
        allTrades.forEach(trade -> assertEquals("N", trade.getExpired()));
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

        Trade expiredTrade1 = createTrade(UUID.randomUUID(), 1, "COUNTER_1", "BOOK_1", lastWeek, "N");
        Trade expiredTrade2 = createTrade(UUID.randomUUID(), 1, "COUNTER_2", "BOOK_2", yesterday, "N");
        Trade validTrade = createTrade(UUID.randomUUID(), 1, "COUNTER_3", "BOOK_3", today.plusDays(1), "N");

        tradeRepository.save(expiredTrade1);
        tradeRepository.save(expiredTrade2);
        tradeRepository.save(validTrade);

        int expiredCount = tradeExpiryScheduler.expireMaturedTrades();

        assertEquals(2, expiredCount);

        Trade updatedExpiredTrade1 = tradeRepository.findById(expiredTrade1.getTradeId()).orElse(null);
        assertNotNull(updatedExpiredTrade1);
        assertEquals("Y", updatedExpiredTrade1.getExpired());

        Trade updatedExpiredTrade2 = tradeRepository.findById(expiredTrade2.getTradeId()).orElse(null);
        assertNotNull(updatedExpiredTrade2);
        assertEquals("Y", updatedExpiredTrade2.getExpired());

        Trade unchangedValidTrade = tradeRepository.findById(validTrade.getTradeId()).orElse(null);
        assertNotNull(unchangedValidTrade);
        assertEquals("N", unchangedValidTrade.getExpired());
    }

    private Trade createTrade(UUID tradeId, int version, String counterPartyId, String bookId, 
                           LocalDate maturityDate, String expired) {
        Trade trade = new Trade();
        trade.setTradeId(tradeId);
        trade.setVersion(version);
        trade.setCounterPartyId(counterPartyId);
        trade.setBookId(bookId);
        trade.setMaturityDate(maturityDate);
        trade.setCreatedDate(LocalDate.now());
        trade.setExpired(expired);
        return trade;
    }
}
