package com.tradestore.repository;

import com.tradestore.domain.valueobject.TradeId;
import com.tradestore.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, TradeId> {

    // Optimized query for finding trades to expire with pagination
    @Query("SELECT t FROM Trade t WHERE t.maturityDate < :currentDate AND t.expired = false")
    List<Trade> findTradesToExpireBatch(@Param("currentDate") java.time.LocalDate currentDate, 
                                       Pageable pageable);

    // Pagination support for large datasets
    @Query("SELECT t FROM Trade t WHERE t.counterPartyId.value = :counterPartyId")
    Page<Trade> findByCounterPartyId(@Param("counterPartyId") String counterPartyId, 
                                    Pageable pageable);

    // Count queries for monitoring
    @Query("SELECT COUNT(t) FROM Trade t WHERE t.expired = :expired")
    long countByExpired(@Param("expired") boolean expired);

}
