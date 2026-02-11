package com.tradestore.repository;

import com.tradestore.entity.AuditMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditMessageRepository extends MongoRepository<AuditMessage, String> {
}
