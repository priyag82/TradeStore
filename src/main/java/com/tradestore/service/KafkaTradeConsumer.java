package com.tradestore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradestore.dto.TradeMessage;
import com.tradestore.entity.AuditMessage;
import com.tradestore.entity.Trade;
import com.tradestore.exception.InvalidTradeException;
import com.tradestore.repository.AuditMessageRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaTradeConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTradeConsumer.class);
    private static final String TRADE_STORE_TOPIC = "incoming.trade.data";

    @Autowired
    private TradeService tradeService;

    @Autowired
    private AuditMessageRepository auditMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = TRADE_STORE_TOPIC, groupId = "trade-store-group")
    public void consumeTradeMessage(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String messageKey,
                                   ConsumerRecord<String, String> consumerRecord,
                                   Acknowledgment acknowledgment) {

        logger.info("Received message from topic: {}, partition: {}, offset: {}, key: {}", 
                   topic, partition, offset, messageKey);

        AuditMessage auditMessage = createAuditMessage(message, topic, partition, offset);
        
        try {
            TradeMessage tradeMessage = objectMapper.readValue(message, TradeMessage.class);
            Trade trade = convertToTrade(tradeMessage);
            
            tradeService.processTrade(trade);
            
            auditMessage.setStatus("PROCESSED");
            auditMessage.setProcessedAt(LocalDateTime.now());
            
            logger.info("Successfully processed trade: {}", trade.getTradeId());
            
        } catch (Exception e) {
            logger.error("Error processing trade message: {}", e.getMessage(), e);
            
            auditMessage.setStatus("FAILED");
            auditMessage.setErrorMessage(e.getMessage());
            auditMessage.setProcessedAt(LocalDateTime.now());
            
            if (e instanceof InvalidTradeException) {
                logger.warn("Invalid trade rejected: {}", e.getMessage());
            } else {
                logger.error("Unexpected error processing trade message", e);
            }
        } finally {
            auditMessageRepository.save(auditMessage);
            acknowledgment.acknowledge();
            logger.debug("Acknowledged message at offset: {}", offset);
        }
    }

    private AuditMessage createAuditMessage(String message, String topic, int partition, long offset) {
        String messageId = UUID.randomUUID().toString();
        LocalDateTime receivedAt = LocalDateTime.now();
        
        return new AuditMessage(messageId, topic, partition, offset, message, receivedAt);
    }

    private Trade convertToTrade(TradeMessage tradeMessage) {
        Trade trade = new Trade();
        trade.setTradeId(com.tradestore.domain.valueobject.TradeId.from(tradeMessage.getTradeId().toString()));
        trade.setVersion(tradeMessage.getVersion());
        trade.setCounterPartyId(com.tradestore.domain.valueobject.CounterPartyId.from(tradeMessage.getCounterPartyId()));
        trade.setBookId(tradeMessage.getBookId());
        trade.setMaturityDate(tradeMessage.getMaturityDate());
        trade.setCreatedDate(tradeMessage.getCreatedDate());
        trade.setExpired(tradeMessage.getExpired() != null ? 
            (tradeMessage.getExpired().equalsIgnoreCase("Y") || tradeMessage.getExpired().equalsIgnoreCase("true")) : false);
        
        return trade;
    }
}
