package com.tradestore.entity;

import com.tradestore.domain.valueobject.TradeId;
import com.tradestore.domain.valueobject.CounterPartyId;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "trades", indexes = {
    @Index(name = "idx_trade_id", columnList = "trade_id"),
    @Index(name = "idx_maturity_date", columnList = "maturity_date"),
    @Index(name = "idx_expired", columnList = "expired"),
    @Index(name = "idx_counter_party", columnList = "counter_party_id"),
    @Index(name = "idx_book_id", columnList = "book_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Trade {

    @EmbeddedId
    private TradeId tradeId;

    @Column(name = "version", nullable = false)
    @Min(value = 1, message = "Version must be at least 1")
    @NotNull(message = "Version is required")
    @Version
    private Integer version;

    @Embedded
    @Column(nullable = false)
    private CounterPartyId counterPartyId;

    @Column(name = "book_id", nullable = false, length = 50)
    @NotBlank(message = "Book ID is required")
    @Size(max = 50, message = "Book ID must not exceed 50 characters")
    private String bookId;

    @Column(name = "maturity_date", nullable = false)
    @NotNull(message = "Maturity date is required")
    @Future(message = "Maturity date must be in the future")
    private LocalDate maturityDate;

    @Column(name = "created_date", nullable = false)
    @NotNull(message = "Created date is required")
    private LocalDate createdDate;

    @Column(name = "expired", nullable = false)
    private boolean expired = false;

    @CreatedDate
    @Column(name = "timestamp")
    private LocalDate timestamp;

    @LastModifiedDate
    @Column(name = "last_updated")
    private LocalDate lastUpdated;

    public Trade() {}

    public Trade(TradeId tradeId, Integer version, CounterPartyId counterPartyId, String bookId, 
                 LocalDate maturityDate, LocalDate createdDate, boolean expired) {
        this.tradeId = tradeId;
        this.version = version;
        this.counterPartyId = counterPartyId;
        this.bookId = bookId;
        this.maturityDate = maturityDate;
        this.createdDate = createdDate;
        this.expired = expired;
        validateMaturityDate();
    }

    private void validateMaturityDate() {
        if (maturityDate != null && maturityDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Trade maturity date cannot be before today");
        }
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
        validateMaturityDate();
    }

    public TradeId getTradeId() {
        return tradeId;
    }

    public void setTradeId(TradeId tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public CounterPartyId getCounterPartyId() {
        return counterPartyId;
    }

    public void setCounterPartyId(CounterPartyId counterPartyId) {
        this.counterPartyId = counterPartyId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isExpired() {
        return expired;
    }

    public boolean getExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "tradeId=" + tradeId +
                ", version=" + version +
                ", counterPartyId='" + counterPartyId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", maturityDate=" + maturityDate +
                ", createdDate=" + createdDate +
                ", expired='" + expired + '\'' +
                '}';
    }
}
