package com.ra.base_spring_boot.repository.account;

import com.ra.base_spring_boot.model.Account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IAccountRepository extends JpaRepository<Account, Integer> {
        Optional<Account> findByUsername(String username);

        Optional<Account> findByEmail(String email);

        boolean existsByUsername(String username);

        boolean existsByEmail(String email);

        // Standard findByUsername is enough as Roles and RolePermissions are EAGER

        // Standard findByEmail is enough as Roles and RolePermissions are EAGER

        @Query("""

                            SELECT DISTINCT a
                            FROM Account a
                            LEFT JOIN FETCH a.accountRoles ar
                            LEFT JOIN FETCH ar.role r
                            WHERE a.email = :email
                        """)
        Optional<Account> findByEmailWithRoles(@Param("email") String email);

        @Query("""
                        SELECT DISTINCT a
                        FROM Account a
                        LEFT JOIN FETCH a.accountRoles ar
                        LEFT JOIN FETCH ar.role r
                        LEFT JOIN FETCH r.rolePermissions rp
                        LEFT JOIN FETCH rp.permission
                        WHERE a.username = :username
                        """)
        Optional<Account> findByUsernameWithRolesAndPermissions(@Param("username") String username);

        @Query("SELECT a FROM Account a WHERE " +
                        "(:search IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "NOT EXISTS (SELECT ar FROM AccountRole ar JOIN ar.role r WHERE ar.account = a AND r.roleCode = 'ROLE_ADMIN')")
        Page<Account> findAllWithFilter(@Param("search") String search,
                        Pageable pageable);

        @Query("SELECT a FROM Account a WHERE " +
                        "(:search IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "EXISTS (SELECT ar FROM AccountRole ar JOIN ar.role r WHERE ar.account = a AND r.roleCode = 'ROLE_EDITOR')")
        Page<Account> findAllEditorsWithFilter(@Param("search") String search,
                        Pageable pageable);

        long countByCreatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
}