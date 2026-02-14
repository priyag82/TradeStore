package com.tradestore.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import java.util.regex.Pattern;

public class SensitiveDataMaskingFilter extends Filter<ILoggingEvent> {

    private static final Pattern UUID_PATTERN = Pattern.compile("\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b");
    private static final Pattern COUNTER_PARTY_ID_PATTERN = Pattern.compile("\"counterPartyId\":\"([^\"]+)\"");
    private static final Pattern BOOK_ID_PATTERN = Pattern.compile("\"bookId\":\"([^\"]+)\"");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|pwd|secret|key)\\s*[:=]\\s*[^\\s,}]+");
    
    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getFormattedMessage() != null) {
            String maskedMessage = maskSensitiveData(event.getFormattedMessage());
            
            if (!maskedMessage.equals(event.getFormattedMessage())) {
                try {
                    var field = event.getClass().getDeclaredField("formattedMessage");
                    field.setAccessible(true);
                    field.set(event, maskedMessage);
                } catch (Exception e) {
                    // If reflection fails, we'll just log the original message
                    // In a production environment, you might want to handle this differently
                }
            }
        }
        return FilterReply.ACCEPT;
    }

    private String maskSensitiveData(String message) {
        if (message == null) {
            return null;
        }

        // Mask UUIDs (Trade IDs)
        message = UUID_PATTERN.matcher(message).replaceAll("****-****-****-****-****");
        
        // Mask counter party IDs
        message = COUNTER_PARTY_ID_PATTERN.matcher(message).replaceAll("\"counterPartyId\":\"****\"");
        
        // Mask book IDs
        message = BOOK_ID_PATTERN.matcher(message).replaceAll("\"bookId\":\"****\"");
        
        // Mask passwords and secrets
        message = PASSWORD_PATTERN.matcher(message).replaceAll("$1=****");
        
        return message;
    }
}
