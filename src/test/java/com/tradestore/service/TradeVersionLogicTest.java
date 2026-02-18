package com.tradestore.service;

import com.tradestore.domain.valueobject.CounterPartyId;
import com.tradestore.domain.valueobject.TradeId;
import com.tradestore.entity.Trade;
import com.tradestore.exception.VersionConflictException;
import com.tradestore.repository.TradeRepository;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeVersionLogicTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private Counter rejectedTradesCounter;

    @Mock
    private Counter processedTradesCounter;

    @Mock
    private Counter expiredTradesCounter;
    
    @Mock
    private TransactionOperations transactionTemplate;

    @InjectMocks
    private TradeService tradeService;

    private TradeId testTradeId;
    private CounterPartyId testCounterPartyId;
    private LocalDate today;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        testTradeId = TradeId.generate();
        testCounterPartyId = CounterPartyId.from("CP-1");
        today = LocalDate.now();
        futureDate = today.plusMonths(6);
        
        // Mock TransactionTemplate to execute the function directly without TransactionStatus
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            // Execute the callback directly without mocking TransactionStatus
            org.springframework.transaction.support.TransactionCallback<Trade> callback = 
                invocation.getArgument(0);
            try {
                return callback.doInTransaction(null);
            } catch (RuntimeException e) {
                throw e;
            }
        });
    }

    @Test
    void shouldCreateNewTradeWhenNoExistingTrade() {
        // Given
        Trade newTrade = new Trade(testTradeId, 1, testCounterPartyId, "B1", futureDate, today, false);
        when(tradeRepository.findById(testTradeId)).thenReturn(Optional.empty());
        when(tradeRepository.save(any(Trade.class))).thenReturn(newTrade);

        // When
        Trade result = tradeService.processTrade(newTrade);

        // Then
        assertNotNull(result);
        verify(tradeRepository).save(newTrade);
    }

    @Test
    void shouldRejectLowerVersionTrade() {
        // Given - existing trade with version 2
        Trade existingTrade = new Trade(testTradeId, 2, testCounterPartyId, "B1", futureDate, today, false);
        Trade lowerVersionTrade = new Trade(testTradeId, 1, testCounterPartyId, "B1", futureDate, today, false);
        
        when(tradeRepository.findById(testTradeId)).thenReturn(Optional.of(existingTrade));

        // When & Then
        VersionConflictException exception = assertThrows(VersionConflictException.class, 
            () -> tradeService.processTrade(lowerVersionTrade));
        
        assertTrue(exception.getMessage().contains("version 1 is lower than existing version 2"));
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void shouldReplaceTradeWithSameVersion() {
        // Given - existing trade with version 1
        Trade existingTrade = new Trade(testTradeId, 1, testCounterPartyId, "B1", futureDate, today, false);
        Trade sameVersionTrade = new Trade(testTradeId, 1, testCounterPartyId, "B2", futureDate, today, false); // Different book ID
        
        when(tradeRepository.findById(testTradeId)).thenReturn(Optional.of(existingTrade));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> {
            Trade savedTrade = invocation.getArgument(0);
            return savedTrade; // Return the same object that was saved
        });

        // When
        Trade result = tradeService.processTrade(sameVersionTrade);

        // Then
        assertNotNull(result);
        verify(tradeRepository).save(any(Trade.class)); // Just verify save was called with any Trade object
    }

    @Test
    void shouldAcceptHigherVersionTrade() {
        // Given - existing trade with version 1
        Trade existingTrade = new Trade(testTradeId, 1, testCounterPartyId, "B1", futureDate, today, false);
        Trade higherVersionTrade = new Trade(testTradeId, 2, testCounterPartyId, "B1", futureDate, today, false);
        
        when(tradeRepository.findById(testTradeId)).thenReturn(Optional.of(existingTrade));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> {
            Trade savedTrade = invocation.getArgument(0);
            return savedTrade; // Return the same object that was saved
        });

        // When
        Trade result = tradeService.processTrade(higherVersionTrade);

        // Then
        assertNotNull(result);
        verify(tradeRepository).save(any(Trade.class)); // Just verify save was called with any Trade object
    }

    @Test
    void shouldRejectTradeWithPastMaturityDate() {
        // Given
        LocalDate pastDate = today.minusDays(1);
        Trade trade = new Trade(testTradeId, 1, testCounterPartyId, "B1", pastDate, today, false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> tradeService.processTrade(trade));
        
        assertTrue(exception.getMessage().contains("maturity date cannot be before today"));
    }
}
