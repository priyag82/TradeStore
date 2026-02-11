package com.tradestore.service;

import com.tradestore.entity.Trade;
import com.tradestore.exception.InvalidTradeException;
import com.tradestore.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeService Validation Tests")
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private TradeService tradeService;

    private Trade validTrade;
    private Trade expiredTrade;
    private Trade lowerVersionTrade;
    private Trade sameVersionTrade;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(30);
        LocalDate pastDate = today.minusDays(10);

        validTrade = new Trade();
        validTrade.setTradeId(UUID.randomUUID());
        validTrade.setVersion(1);
        validTrade.setCounterPartyId("CP-001");
        validTrade.setBookId("BOOK-001");
        validTrade.setMaturityDate(futureDate);
        validTrade.setCreatedDate(today);
        validTrade.setExpired("N");

        expiredTrade = new Trade();
        expiredTrade.setTradeId(UUID.randomUUID());
        expiredTrade.setVersion(1);
        expiredTrade.setCounterPartyId("CP-002");
        expiredTrade.setBookId("BOOK-002");
        expiredTrade.setMaturityDate(pastDate);
        expiredTrade.setCreatedDate(today);
        expiredTrade.setExpired("N");

        lowerVersionTrade = new Trade();
        lowerVersionTrade.setTradeId(UUID.randomUUID());
        lowerVersionTrade.setVersion(1);
        lowerVersionTrade.setCounterPartyId("CP-003");
        lowerVersionTrade.setBookId("BOOK-003");
        lowerVersionTrade.setMaturityDate(futureDate);
        lowerVersionTrade.setCreatedDate(today);
        lowerVersionTrade.setExpired("N");

        sameVersionTrade = new Trade();
        sameVersionTrade.setTradeId(lowerVersionTrade.getTradeId());
        sameVersionTrade.setVersion(1);
        sameVersionTrade.setCounterPartyId("CP-003");
        sameVersionTrade.setBookId("BOOK-003");
        sameVersionTrade.setMaturityDate(futureDate);
        sameVersionTrade.setCreatedDate(today);
        sameVersionTrade.setExpired("N");
    }

    @Test
    @DisplayName("Should accept valid trade with future maturity date")
    void testValidTradeAcceptance() {
        when(tradeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(tradeRepository.save(any(Trade.class))).thenReturn(validTrade);

        Trade result = tradeService.processTrade(validTrade);

        assertNotNull(result);
        verify(tradeRepository).save(validTrade);
    }

    @Test
    @DisplayName("Should reject trade with past maturity date")
    void testPastMaturityDateRejection() {
        InvalidTradeException exception = assertThrows(
            InvalidTradeException.class,
            () -> tradeService.processTrade(expiredTrade)
        );

        assertEquals("Trade maturity date cannot be before today", exception.getMessage());
        verify(tradeRepository, never()).save(any(Trade.class));
    }

    @Test
    @DisplayName("Should reject trade with lower version than existing")
    void testLowerVersionRejection() {
        Trade existingTrade = new Trade();
        existingTrade.setTradeId(lowerVersionTrade.getTradeId());
        existingTrade.setVersion(2);
        existingTrade.setCounterPartyId("CP-003");
        existingTrade.setBookId("BOOK-003");
        existingTrade.setMaturityDate(lowerVersionTrade.getMaturityDate());
        existingTrade.setCreatedDate(lowerVersionTrade.getCreatedDate());
        existingTrade.setExpired("N");

        when(tradeRepository.findById(lowerVersionTrade.getTradeId()))
            .thenReturn(Optional.of(existingTrade));

        InvalidTradeException exception = assertThrows(
            InvalidTradeException.class,
            () -> tradeService.processTrade(lowerVersionTrade)
        );

        assertEquals("Trade version 1 is lower than existing version 2", exception.getMessage());
        verify(tradeRepository, never()).save(any(Trade.class));
    }

    @Test
    @DisplayName("Should replace existing trade when version is same")
    void testSameVersionReplacement() {
        Trade existingTrade = new Trade();
        existingTrade.setTradeId(sameVersionTrade.getTradeId());
        existingTrade.setVersion(1);
        existingTrade.setCounterPartyId("CP-003");
        existingTrade.setBookId("BOOK-003");
        existingTrade.setMaturityDate(sameVersionTrade.getMaturityDate());
        existingTrade.setCreatedDate(sameVersionTrade.getCreatedDate());
        existingTrade.setExpired("N");

        when(tradeRepository.findById(sameVersionTrade.getTradeId()))
            .thenReturn(Optional.of(existingTrade));
        when(tradeRepository.save(any(Trade.class))).thenReturn(sameVersionTrade);

        Trade result = tradeService.processTrade(sameVersionTrade);

        assertNotNull(result);
        verify(tradeRepository).save(sameVersionTrade);
    }

    @Test
    @DisplayName("Should accept trade with higher version than existing")
    void testHigherVersionAcceptance() {
        Trade higherVersionTrade = new Trade();
        higherVersionTrade.setTradeId(lowerVersionTrade.getTradeId());
        higherVersionTrade.setVersion(3);
        higherVersionTrade.setCounterPartyId("CP-003");
        higherVersionTrade.setBookId("BOOK-003");
        higherVersionTrade.setMaturityDate(lowerVersionTrade.getMaturityDate());
        higherVersionTrade.setCreatedDate(lowerVersionTrade.getCreatedDate());
        higherVersionTrade.setExpired("N");

        Trade existingTrade = new Trade();
        existingTrade.setTradeId(lowerVersionTrade.getTradeId());
        existingTrade.setVersion(2);
        existingTrade.setCounterPartyId("CP-003");
        existingTrade.setBookId("BOOK-003");
        existingTrade.setMaturityDate(lowerVersionTrade.getMaturityDate());
        existingTrade.setCreatedDate(lowerVersionTrade.getCreatedDate());
        existingTrade.setExpired("N");

        when(tradeRepository.findById(higherVersionTrade.getTradeId()))
            .thenReturn(Optional.of(existingTrade));
        when(tradeRepository.save(any(Trade.class))).thenReturn(higherVersionTrade);

        Trade result = tradeService.processTrade(higherVersionTrade);

        assertNotNull(result);
        verify(tradeRepository).save(higherVersionTrade);
    }

    @Test
    @DisplayName("Should mark expired trades as expired")
    void testMarkExpiredTrades() {
        Trade expiredTradeToMark = new Trade();
        expiredTradeToMark.setTradeId(UUID.randomUUID());
        expiredTradeToMark.setVersion(1);
        expiredTradeToMark.setCounterPartyId("CP-004");
        expiredTradeToMark.setBookId("BOOK-004");
        expiredTradeToMark.setMaturityDate(LocalDate.now().minusDays(1));
        expiredTradeToMark.setCreatedDate(LocalDate.now().minusDays(30));
        expiredTradeToMark.setExpired("N");

        when(tradeRepository.findByMaturityDateBeforeAndExpired(any(LocalDate.class), eq("N")))
            .thenReturn(java.util.Arrays.asList(expiredTradeToMark));

        tradeService.markExpiredTrades();

        verify(tradeRepository).save(expiredTradeToMark);
        assertEquals("Y", expiredTradeToMark.getExpired());
    }

    @Test
    @DisplayName("Should not mark non-expired trades")
    void testDoNotMarkNonExpiredTrades() {
        when(tradeRepository.findByMaturityDateBeforeAndExpired(any(LocalDate.class), eq("N")))
            .thenReturn(java.util.Collections.emptyList());

        tradeService.markExpiredTrades();

        verify(tradeRepository, never()).save(any(Trade.class));
    }
}
