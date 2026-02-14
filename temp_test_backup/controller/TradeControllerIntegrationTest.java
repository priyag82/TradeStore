package com.tradestore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradestore.entity.Trade;
import com.tradestore.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TradeControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllTrades_shouldReturnAllTrades() {
        // Given
        Trade trade1 = new Trade(UUID.randomUUID(), 1, "COUNTER_PARTY_1", "BOOK_1", 
                               LocalDate.now().plusYears(1), LocalDate.now(), false);
        Trade trade2 = new Trade(UUID.randomUUID(), 1, "COUNTER_PARTY_2", "BOOK_2", 
                               LocalDate.now().plusYears(2), LocalDate.now(), false);
        tradeRepository.saveAll(Arrays.asList(trade1, trade2));

        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/api/trades", String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        System.out.println("Response: " + response.getBody());
    }

    @Test
    void testGetTrade_shouldReturnTradeWhenExists() {
        // Given
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(tradeId, 1, "COUNTER_PARTY_1", "BOOK_1", 
                              LocalDate.now().plusYears(1), LocalDate.now(), false);
        tradeRepository.save(trade);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/api/trades/" + tradeId, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        System.out.println("Response: " + response.getBody());
    }

    @Test
    void testGetTrade_shouldReturnNotFoundWhenNotExists() {
        // When
        UUID nonExistentId = UUID.randomUUID();
        ResponseEntity<String> response = restTemplate.getForEntity("/api/trades/" + nonExistentId, String.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateTrade_shouldReturnCreatedWhenValid() throws Exception {
        // Given
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(tradeId, 1, "COUNTER_PARTY_1", "BOOK_1", 
                              LocalDate.now().plusYears(1), LocalDate.now(), false);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity("/api/trades", trade, String.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        System.out.println("Response: " + response.getBody());
    }

    @Test
    void testCreateTrade_shouldReturnBadRequestWhenInvalid() throws Exception {
        // Given
        Trade invalidTrade = new Trade(UUID.randomUUID(), 1, "COUNTER_PARTY_1", "BOOK_1", 
                                   LocalDate.now().minusDays(1), LocalDate.now(), false); // Past maturity date

        // When
        ResponseEntity<String> response = restTemplate.postForEntity("/api/trades", invalidTrade, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
