package com.ra.base_spring_boot.repository.account;

import com.ra.base_spring_boot.model.AccountRole;
import com.ra.base_spring_boot.model.constants.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRoleRepo extends JpaRepository<AccountRole, Integer> {

    @Query("""
            select r.roleCode from AccountRole ar
            join ar.role r
            where ar.account.accountId = :accountId
            """)
    List<RoleName> findRoleCodesByAccountId(@Param("accountId") Integer accountId);

    @Query("SELECT ar FROM AccountRole ar WHERE ar.account.accountId = :accountId AND ar.isPrimary = true")
    Optional<AccountRole> findPrimaryRoleByAccountId(@Param("accountId") Integer accountId);

    @Query("SELECT ar FROM AccountRole ar WHERE ar.account.accountId = :accountId AND ar.role.roleCode = :roleCode")
    Optional<AccountRole> findByAccountIdAndRoleCode(@Param("accountId") Integer accountId,
            @Param("roleCode") RoleName roleCode);

    void deleteAllByAccount(com.ra.base_spring_boot.model.Account account);
}