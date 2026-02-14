package com.tradestore.repository;

import com.tradestore.domain.valueobject.TradeId;
import com.tradestore.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

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

    // Bulk update for expiry (more efficient than individual saves)
    @Modifying
    @Query("UPDATE Trade t SET t.expired = true WHERE t.maturityDate < :currentDate AND t.expired = false")
    int bulkMarkAsExpired(@Param("currentDate") java.time.LocalDate currentDate);

    // Find trades by book ID with pagination
    @Query("SELECT t FROM Trade t WHERE t.bookId = :bookId")
    Page<Trade> findByBookId(@Param("bookId") String bookId, Pageable pageable);

    // Find trades by maturity date range
    @Query("SELECT t FROM Trade t WHERE t.maturityDate BETWEEN :startDate AND :endDate")
    Page<Trade> findByMaturityDateBetween(@Param("startDate") java.time.LocalDate startDate,
                                         @Param("endDate") java.time.LocalDate endDate,
                                         Pageable pageable);

    // Find non-expired trades
    @Query("SELECT t FROM Trade t WHERE t.expired = false ORDER BY t.createdDate DESC")
    Page<Trade> findActiveTrades(Pageable pageable);

    // Find by TradeId value object
    boolean existsByTradeId(TradeId tradeId);
    
    // Find trades by maturity date before and expired flag (for test compatibility)
    @Query("SELECT t FROM Trade t WHERE t.maturityDate < :currentDate AND t.expired = :expired")
    List<Trade> findByMaturityDateBeforeAndExpired(@Param("currentDate") java.time.LocalDate currentDate, 
                                                  @Param("expired") boolean expired);
}
