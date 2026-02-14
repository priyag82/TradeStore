package com.tradestore.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "audit_messages")
public class AuditMessage {

    @Id
    private String id;

    @Field("message_id")
    private String messageId;

    @Field("topic")
    private String topic;

    @Field("partition")
    private Integer partition;

    @Field("offset")
    private Long offset;

    @Field("message_content")
    private String messageContent;

    @Field("received_at")
    private LocalDateTime receivedAt;

    @Field("processed_at")
    private LocalDateTime processedAt;

    @Field("status")
    private String status;

    @Field("error_message")
    private String errorMessage;

    public AuditMessage(String messageId, String topic, Integer partition, Long offset,
                       String messageContent, LocalDateTime receivedAt) {
        this.messageId = messageId;
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.messageContent = messageContent;
        this.receivedAt = receivedAt;
        this.status = "RECEIVED";
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "AuditMessage{" +
                "id='" + id + '\'' +
                ", messageId='" + messageId + '\'' +
                ", topic='" + topic + '\'' +
                ", partition=" + partition +
                ", offset=" + offset +
                ", receivedAt=" + receivedAt +
                ", processedAt=" + processedAt +
                ", status='" + status + '\'' +
                '}';
    }
}
