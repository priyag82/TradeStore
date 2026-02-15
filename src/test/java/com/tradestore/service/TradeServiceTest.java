package com.tradestore.service;

import com.tradestore.entity.Trade;
import com.tradestore.exception.VersionConflictException;
import com.tradestore.repository.TradeRepository;
import com.tradestore.domain.valueobject.TradeId;
import com.tradestore.domain.valueobject.CounterPartyId;
import io.micrometer.core.instrument.Counter;
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
    
    @Mock
    private Counter rejectedTradesCounter;
    
    @Mock
    private Counter processedTradesCounter;
    
    @Mock
    private Counter expiredTradesCounter;

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
        validTrade.setTradeId(TradeId.generate());
        validTrade.setVersion(1);
        validTrade.setCounterPartyId(CounterPartyId.from("CP-001"));
        validTrade.setBookId("BOOK-001");
        validTrade.setMaturityDate(futureDate);
        validTrade.setCreatedDate(today);
        validTrade.setExpired(false);

        expiredTrade = new Trade();
        expiredTrade.setTradeId(TradeId.generate());
        expiredTrade.setVersion(1);
        expiredTrade.setCounterPartyId(CounterPartyId.from("CP-002"));
        expiredTrade.setBookId("BOOK-002");
        expiredTrade.setCreatedDate(today);
        expiredTrade.setExpired(false);
        // Use reflection to set maturity date without validation for testing
        try {
            java.lang.reflect.Field field = Trade.class.getDeclaredField("maturityDate");
            field.setAccessible(true);
            field.set(expiredTrade, pastDate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        lowerVersionTrade = new Trade();
        lowerVersionTrade.setTradeId(TradeId.generate());
        lowerVersionTrade.setVersion(1);
        lowerVersionTrade.setCounterPartyId(CounterPartyId.from("CP-003"));
        lowerVersionTrade.setBookId("BOOK-003");
        lowerVersionTrade.setMaturityDate(futureDate);
        lowerVersionTrade.setCreatedDate(today);
        lowerVersionTrade.setExpired(false);

        sameVersionTrade = new Trade();
        sameVersionTrade.setTradeId(lowerVersionTrade.getTradeId());
        sameVersionTrade.setVersion(1);
        sameVersionTrade.setCounterPartyId(CounterPartyId.from("CP-003"));
        sameVersionTrade.setBookId("BOOK-003");
        sameVersionTrade.setMaturityDate(futureDate);
        sameVersionTrade.setCreatedDate(today);
        sameVersionTrade.setExpired(false);
    }

    @Test
    @DisplayName("Should accept valid trade with future maturity date")
    void testValidTradeAcceptance() {
        when(tradeRepository.findById(any(TradeId.class))).thenReturn(Optional.empty());
        when(tradeRepository.save(any(Trade.class))).thenReturn(validTrade);

        Trade result = tradeService.processTrade(validTrade);

        assertNotNull(result);
        verify(tradeRepository).save(validTrade);
    }

    @Test
    @DisplayName("Should reject trade with past maturity date")
    void testPastMaturityDateRejection() {
        // Create a trade with past maturity date using reflection to bypass validation
        Trade invalidTrade = new Trade();
        invalidTrade.setTradeId(TradeId.generate());
        invalidTrade.setVersion(1);
        invalidTrade.setCounterPartyId(CounterPartyId.from("CP-002"));
        invalidTrade.setBookId("BOOK-002");
        invalidTrade.setCreatedDate(LocalDate.now());
        invalidTrade.setExpired(false);
        // Use reflection to set past maturity date
        try {
            java.lang.reflect.Field field = Trade.class.getDeclaredField("maturityDate");
            field.setAccessible(true);
            field.set(invalidTrade, LocalDate.now().minusDays(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> tradeService.processTrade(invalidTrade)
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
        existingTrade.setCounterPartyId(CounterPartyId.from("CP-003"));
        existingTrade.setBookId("BOOK-003");
        existingTrade.setMaturityDate(lowerVersionTrade.getMaturityDate());
        existingTrade.setCreatedDate(lowerVersionTrade.getCreatedDate());
        existingTrade.setExpired(false);

        when(tradeRepository.findById(lowerVersionTrade.getTradeId()))
            .thenReturn(Optional.of(existingTrade));

        VersionConflictException exception = assertThrows(
            VersionConflictException.class,
            () -> tradeService.processTrade(lowerVersionTrade)
        );

        assertEquals("Trade version 1 is lower than existing version 2 - REJECTED", exception.getMessage());
        verify(tradeRepository, never()).save(any(Trade.class));
    }

    @Test
    @DisplayName("Should replace existing trade when version is same")
    void testSameVersionReplacement() {
        Trade existingTrade = new Trade();
        existingTrade.setTradeId(sameVersionTrade.getTradeId());
        existingTrade.setVersion(1);
        existingTrade.setCounterPartyId(CounterPartyId.from("CP-003"));
        existingTrade.setBookId("BOOK-003");
        existingTrade.setMaturityDate(sameVersionTrade.getMaturityDate());
        existingTrade.setCreatedDate(sameVersionTrade.getCreatedDate());
        existingTrade.setExpired(false);

        when(tradeRepository.findById(sameVersionTrade.getTradeId()))
            .thenReturn(Optional.of(existingTrade));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> {
            Trade savedTrade = invocation.getArgument(0);
            return savedTrade; // Return the same object that was saved
        });

        Trade result = tradeService.processTrade(sameVersionTrade);

        assertNotNull(result);
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    @DisplayName("Should accept trade with higher version than existing")
    void testHigherVersionAcceptance() {
        Trade higherVersionTrade = new Trade();
        higherVersionTrade.setTradeId(lowerVersionTrade.getTradeId());
        higherVersionTrade.setVersion(3);
        higherVersionTrade.setCounterPartyId(CounterPartyId.from("CP-003"));
        higherVersionTrade.setBookId("BOOK-003");
        higherVersionTrade.setMaturityDate(lowerVersionTrade.getMaturityDate());
        higherVersionTrade.setCreatedDate(lowerVersionTrade.getCreatedDate());
        higherVersionTrade.setExpired(false);

        Trade existingTrade = new Trade();
        existingTrade.setTradeId(lowerVersionTrade.getTradeId());
        existingTrade.setVersion(2);
        existingTrade.setCounterPartyId(CounterPartyId.from("CP-003"));
        existingTrade.setBookId("BOOK-003");
        existingTrade.setMaturityDate(lowerVersionTrade.getMaturityDate());
        existingTrade.setCreatedDate(lowerVersionTrade.getCreatedDate());
        existingTrade.setExpired(false);

        when(tradeRepository.findById(higherVersionTrade.getTradeId()))
            .thenReturn(Optional.of(existingTrade));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> {
            Trade savedTrade = invocation.getArgument(0);
            return savedTrade; // Return the same object that was saved
        });

        Trade result = tradeService.processTrade(higherVersionTrade);

        assertNotNull(result);
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    @DisplayName("Should mark expired trades as expired")
    void testMarkExpiredTrades() {
        Trade expiredTradeToMark = new Trade();
        expiredTradeToMark.setTradeId(TradeId.generate());
        expiredTradeToMark.setVersion(1);
        expiredTradeToMark.setCounterPartyId(CounterPartyId.from("CP-004"));
        expiredTradeToMark.setBookId("BOOK-004");
        expiredTradeToMark.setCreatedDate(LocalDate.now().minusDays(30));
        expiredTradeToMark.setExpired(false);
        // Use reflection to set maturity date without validation for testing
        try {
            java.lang.reflect.Field field = Trade.class.getDeclaredField("maturityDate");
            field.setAccessible(true);
            field.set(expiredTradeToMark, LocalDate.now().minusDays(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(tradeRepository.findTradesToExpireBatch(any(LocalDate.class), any()))
            .thenReturn(java.util.Arrays.asList(expiredTradeToMark));

        tradeService.markExpiredTrades();

        verify(tradeRepository).save(expiredTradeToMark);
        assertTrue(expiredTradeToMark.isExpired());
    }

    @Test
    @DisplayName("Should not mark non-expired trades")
    void testDoNotMarkNonExpiredTrades() {
        when(tradeRepository.findTradesToExpireBatch(any(LocalDate.class), any()))
            .thenReturn(java.util.Collections.emptyList());

        tradeService.markExpiredTrades();

        verify(tradeRepository, never()).save(any(Trade.class));
    }
}
