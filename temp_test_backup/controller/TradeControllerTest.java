package com.tradestore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradestore.entity.Trade;
import com.tradestore.service.TradeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradeController.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllTrades_shouldReturnAllTrades() throws Exception {
        // Given
        List<Trade> trades = Arrays.asList(
            new Trade(UUID.randomUUID(), 1, "COUNTER_PARTY_1", "BOOK_1", 
                       LocalDate.now().plusYears(1), LocalDate.now(), false),
            new Trade(UUID.randomUUID(), 1, "COUNTER_PARTY_2", "BOOK_2", 
                       LocalDate.now().plusYears(2), LocalDate.now(), false)
        );
        when(tradeService.getAllTrades()).thenReturn(trades);

        // When & Then
        mockMvc.perform(get("/api/trades"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getTrade_shouldReturnTradeWhenExists() throws Exception {
        // Given
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(tradeId, 1, "COUNTER_PARTY_1", "BOOK_1", 
                              LocalDate.now().plusYears(1), LocalDate.now(), false);
        when(tradeService.getTrade(tradeId)).thenReturn(Optional.of(trade));

        // When & Then
        mockMvc.perform(get("/api/trades/{tradeId}", tradeId))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.tradeId").value(tradeId.toString()))
               .andExpect(jsonPath("$.counterPartyId").value("COUNTER_PARTY_1"))
               .andExpect(jsonPath("$.bookId").value("BOOK_1"));
    }

    @Test
    void getTrade_shouldReturnNotFoundWhenNotExists() throws Exception {
        // Given
        UUID tradeId = UUID.randomUUID();
        when(tradeService.getTrade(tradeId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/trades/{tradeId}", tradeId))
               .andExpect(status().isNotFound());
    }

    @Test
    void createTrade_shouldReturnCreatedWhenValid() throws Exception {
        // Given
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(tradeId, 1, "COUNTER_PARTY_1", "BOOK_1", 
                              LocalDate.now().plusYears(1), LocalDate.now(), false);
        when(tradeService.processTrade(any(Trade.class))).thenReturn(trade);

        // When & Then
        mockMvc.perform(post("/api/trades")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(trade)))
               .andExpect(status().isCreated())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.tradeId").value(tradeId.toString()));
    }

    @Test
    void createTrade_shouldReturnBadRequestWhenInvalid() throws Exception {
        // Given
        Trade invalidTrade = new Trade(UUID.randomUUID(), 1, "COUNTER_PARTY_1", "BOOK_1", 
                                   LocalDate.now().minusDays(1), LocalDate.now(), false); // Past maturity date
        when(tradeService.processTrade(any(Trade.class)))
                .thenThrow(new RuntimeException("Trade maturity date cannot be before today"));

        // When & Then
        mockMvc.perform(post("/api/trades")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(invalidTrade)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void updateTrade_shouldReturnOkWhenValid() throws Exception {
        // Given
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(tradeId, 2, "COUNTER_PARTY_1", "BOOK_1", 
                              LocalDate.now().plusYears(1), LocalDate.now(), false);
        when(tradeService.processTrade(any(Trade.class))).thenReturn(trade);

        // When & Then
        mockMvc.perform(put("/api/trades/{tradeId}", tradeId)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(trade)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void updateTrade_shouldReturnBadRequestWhenIdsDontMatch() throws Exception {
        // Given
        UUID pathId = UUID.randomUUID();
        UUID bodyId = UUID.randomUUID();
        Trade trade = new Trade(bodyId, 2, "COUNTER_PARTY_1", "BOOK_1", 
                              LocalDate.now().plusYears(1), LocalDate.now(), "N");

        // When & Then
        mockMvc.perform(put("/api/trades/{tradeId}", pathId)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(trade)))
               .andExpect(status().isBadRequest());
    }
}
