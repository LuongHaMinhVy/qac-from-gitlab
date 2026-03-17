package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ActivityLogRepo extends JpaRepository<ActivityLog, Integer> {
  @Query("""
          SELECT al
          FROM ActivityLog al
          WHERE (:accountId IS NULL OR al.account.accountId = :accountId)
            AND (:action IS NULL OR al.action = :action)
            AND (
                  :keyword IS NULL
                  OR LOWER(al.details) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
      """)
  Page<ActivityLog> search(
      @Param("accountId") Integer accountId,
      @Param("action") String action,
      @Param("keyword") String keyword,
      Pageable pageable);

  @Query("""
          SELECT COUNT(a)
          FROM ActivityLog a
          WHERE a.action = 'LOGIN'
            AND a.account.accountId = :accountId
      """)
  Long getCountLogin(@Param("accountId") Long accountId);

  @Query("""
          SELECT max(a.createdAt)
          FROM ActivityLog a
          WHERE a.action = 'LOGIN'
            AND a.account.accountId = :accountId
      """)
  LocalDateTime getLastLogin(@Param("accountId") Long accountId);
}
