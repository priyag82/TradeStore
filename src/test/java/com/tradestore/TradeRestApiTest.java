package com.tradestore;

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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TradeRestApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void testGetAllTrades() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/api/trades", String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        System.out.println("✅ GET /api/trades works! Response: " + response.getStatusCode());
    }

    @Test
    void testCreateTrade() {
        // Given
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(tradeId, 1, "COUNTER_PARTY_1", "BOOK_1", 
                              LocalDate.now().plusYears(1), LocalDate.now(), "N");

        // When
        ResponseEntity<String> response = restTemplate.postForEntity("/api/trades", trade, String.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        System.out.println("✅ POST /api/trades works! Response: " + response.getStatusCode());
        
        // Verify trade was saved
        assertTrue(tradeRepository.existsById(tradeId));
        System.out.println("✅ Trade was saved to database!");
    }

    @Test
    void testGetTradeById() {
        // Given
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(tradeId, 1, "COUNTER_PARTY_1", "BOOK_1", 
                              LocalDate.now().plusYears(1), LocalDate.now(), "N");
        tradeRepository.save(trade);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/api/trades/" + tradeId, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        System.out.println("✅ GET /api/trades/{id} works! Response: " + response.getStatusCode());
    }
}
