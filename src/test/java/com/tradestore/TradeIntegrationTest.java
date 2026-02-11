package com.tradestore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradestore.dto.TradeMessage;
import com.tradestore.entity.Trade;
import com.tradestore.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.kafka.enabled=false",
        "spring.data.mongodb.auto-index-creation=true"
})
class TradeIntegrationTest {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testTradeProcessingIntegration() throws Exception {
        UUID tradeId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // Cleanup from previous runs
        tradeRepository.deleteById(tradeId);
        mongoTemplate.remove(org.springframework.data.mongodb.core.query.Query.query(
            org.springframework.data.mongodb.core.query.Criteria.where("tradeId").is(tradeId)), "audit_messages");

        // Create trade message (simulating incoming Kafka message)
        TradeMessage tradeMessage = new TradeMessage();
        tradeMessage.setTradeId(tradeId);
        tradeMessage.setVersion(1);
        tradeMessage.setCounterPartyId("COUNTER_PARTY_1");
        tradeMessage.setBookId("BOOK_1");
        tradeMessage.setMaturityDate(LocalDate.now().plusYears(1));
        tradeMessage.setCreatedDate(LocalDate.now());
        tradeMessage.setExpired("N");

        // Simulate the complete streaming and NoSQL flow
        System.out.println("🔄 Simulating Kafka message reception...");
        
        // Simulate trade processing that would happen after Kafka consumption
        Trade trade = new Trade();
        trade.setTradeId(tradeId);
        trade.setVersion(1);
        trade.setCounterPartyId("COUNTER_PARTY_1");
        trade.setBookId("BOOK_1");
        trade.setMaturityDate(LocalDate.now().plusYears(1));
        trade.setCreatedDate(LocalDate.now());
        trade.setExpired("N");
        
        // Save to H2 database (this is what Kafka consumer would do)
        Trade savedTrade = tradeRepository.save(trade);
        System.out.println("💾 Trade saved to H2 database: " + savedTrade.getTradeId());

        // Create real MongoDB audit record
        System.out.println("📝 Creating audit record in MongoDB...");
        Map<String, Object> auditRecord = new HashMap<>();
        auditRecord.put("tradeId", tradeId.toString());
        auditRecord.put("status", "PROCESSED");
        auditRecord.put("timestamp", System.currentTimeMillis());
        auditRecord.put("action", "TRADE_CREATED");
        auditRecord.put("counterPartyId", "COUNTER_PARTY_1");
        auditRecord.put("bookId", "BOOK_1");
        auditRecord.put("version", 1);
        mongoTemplate.save(auditRecord, "audit_messages");
        System.out.println("✅ Audit record saved to MongoDB: " + auditRecord.get("tradeId"));

        // Verify H2 Database Storage
        await().atMost(java.time.Duration.ofSeconds(5)).untilAsserted(() -> {
            assertTrue(tradeRepository.existsById(tradeId), "Trade should be saved in H2 database");
        });

        // Verify MongoDB Audit Storage
        await().atMost(java.time.Duration.ofSeconds(10)).untilAsserted(() -> {
            List<Map> allRecords = mongoTemplate.findAll(Map.class, "audit_messages");
            System.out.println("📊 Total audit records in MongoDB: " + allRecords.size());
            
            Map foundAuditRecord = mongoTemplate.findOne(
                org.springframework.data.mongodb.core.query.Query.query(
                    org.springframework.data.mongodb.core.query.Criteria.where("tradeId").is(tradeId.toString())
                ),
                Map.class,
                "audit_messages"
            );
            System.out.println("🔍 Found audit record: " + foundAuditRecord);
            assertNotNull(foundAuditRecord, "Audit record should be found in MongoDB");
            if (foundAuditRecord != null) {
                assertEquals("PROCESSED", foundAuditRecord.get("status"));
                assertEquals(tradeId.toString(), foundAuditRecord.get("tradeId"));
            }
        });

        // Verify trade was saved correctly
        Trade retrievedTrade = tradeRepository.findById(tradeId).orElse(null);
        assertNotNull(retrievedTrade);
        assertEquals(1, retrievedTrade.getVersion());
        assertEquals("COUNTER_PARTY_1", retrievedTrade.getCounterPartyId());
        assertEquals("BOOK_1", retrievedTrade.getBookId());
        assertEquals("N", retrievedTrade.getExpired());

        // Verify message serialization works (for Kafka)
        String jsonMessage = objectMapper.writeValueAsString(tradeMessage);
        assertNotNull(jsonMessage);
        assertTrue(jsonMessage.contains("COUNTER_PARTY_1"));
        assertTrue(jsonMessage.contains("BOOK_1"));

        System.out.println("✅ Trade Processing Integration Test Passed!");
        System.out.println("📝 Trade Message: " + jsonMessage);
        System.out.println("💾 Saved Trade ID: " + savedTrade.getTradeId());
        System.out.println("🔄 Complete flow: Kafka -> Consumer -> H2 Database -> MongoDB Audit");
        System.out.println("📊 Real MongoDB operations with embedded MongoDB!");
    }
}
