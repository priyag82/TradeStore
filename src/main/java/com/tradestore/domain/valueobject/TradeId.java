package com.tradestore.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object for Trade ID.
 * Ensures type safety and domain validation for trade identifiers.
 */
@Embeddable
public class TradeId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "trade_id", columnDefinition = "BINARY(16)")
    private UUID value;

    // Default constructor for JPA
    protected TradeId() {
    }

    public TradeId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Trade ID cannot be null");
        }
        this.value = value;
    }

    public static TradeId generate() {
        return new TradeId(UUID.randomUUID());
    }

    @JsonCreator
    public static TradeId from(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("Trade ID string cannot be null or empty");
        }
        try {
            return new TradeId(UUID.fromString(uuidString.trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Trade ID format: " + uuidString, e);
        }
    }

    @JsonValue
    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeId tradeId = (TradeId) o;
        return Objects.equals(value, tradeId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
