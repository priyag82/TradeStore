package com.tradestore.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object for Counter Party ID.
 * Ensures type safety and domain validation for counter party identifiers.
 */
@Embeddable
public class CounterPartyId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "counter_party_id", nullable = false, length = 50)
    private String value;

    // Default constructor for JPA
    protected CounterPartyId() {
    }

    public CounterPartyId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Counter Party ID cannot be null or empty");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("Counter Party ID must not exceed 50 characters");
        }
        this.value = value.trim();
    }

    @JsonCreator
    public static CounterPartyId from(String value) {
        return new CounterPartyId(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CounterPartyId that = (CounterPartyId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
