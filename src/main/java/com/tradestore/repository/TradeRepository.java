package com.tradestore.repository;

import com.tradestore.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TradeRepository extends JpaRepository<Trade, UUID> {

    @Query("SELECT t FROM Trade t WHERE t.maturityDate < :currentDate AND t.expired = :expiredFlag")
    List<Trade> findByMaturityDateBeforeAndExpired(@Param("currentDate") java.time.LocalDate currentDate, 
                                                   @Param("expiredFlag") String expiredFlag);

    boolean existsByTradeId(UUID tradeId);
}
