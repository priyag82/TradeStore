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

    public AuditMessage() {}

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
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
