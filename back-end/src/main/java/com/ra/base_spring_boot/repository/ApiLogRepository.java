package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ApiLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {
        @Query("SELECT l FROM ApiLog l WHERE " +
                        "(:accountId IS NULL OR l.account.accountId = :accountId) AND " +
                        "(:method IS NULL OR l.method = :method) AND " +
                        "(:statusCode IS NULL OR l.statusCode = :statusCode) AND " +
                        "(:endpoint IS NULL OR LOWER(l.endpoint) LIKE LOWER(CONCAT('%', :endpoint, '%'))) AND " +
                        "(:startDate IS NULL OR l.createdAt >= :startDate) AND " +
                        "(:endDate IS NULL OR l.createdAt <= :endDate)")
        Page<ApiLog> searchLogs(
                        @Param("accountId") Integer accountId,
                        @Param("method") String method,
                        @Param("statusCode") Integer statusCode,
                        @Param("endpoint") String endpoint,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);
}
