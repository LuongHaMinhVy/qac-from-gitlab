package com.ra.base_spring_boot.repository.user;

import com.ra.base_spring_boot.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

  @Query("""
          SELECT DISTINCT u
          FROM User u
          JOIN u.account a
          JOIN a.accountRoles ar
          JOIN ar.role r
          WHERE r.roleCode <> com.ra.base_spring_boot.model.constants.RoleName.ROLE_ADMIN
            AND (:email IS NULL OR LOWER(a.email) LIKE LOWER(CONCAT('%', :email, '%')))
            AND (:isActive IS NULL OR a.isActive = :isActive)
            AND (:role IS NULL OR r.roleCode = :role)
          ORDER BY
            CASE WHEN :sort = 'fullName' AND :direction = 'asc' THEN u.fullName END ASC,
            CASE WHEN :sort = 'fullName' AND :direction = 'desc' THEN u.fullName END DESC,
            CASE WHEN :sort = 'email' AND :direction = 'asc' THEN a.email END ASC,
            CASE WHEN :sort = 'email' AND :direction = 'desc' THEN a.email END DESC,
            CASE WHEN :sort = 'username' AND :direction = 'asc' THEN a.username END ASC,
            CASE WHEN :sort = 'username' AND :direction = 'desc' THEN a.username END DESC,
            CASE WHEN :sort = 'createdAt' AND :direction = 'asc' THEN u.createdAt END ASC,
            CASE WHEN :sort = 'createdAt' AND :direction = 'desc' THEN u.createdAt END DESC,
            CASE WHEN :sort = 'updatedAt' AND :direction = 'asc' THEN u.updatedAt END ASC,
            CASE WHEN :sort = 'updatedAt' AND :direction = 'desc' THEN u.updatedAt END DESC
      """)
  List<User> findAllMembersForExport(
      @Param("email") String email,
      @Param("role") com.ra.base_spring_boot.model.constants.RoleName role,
      @Param("isActive") Boolean isActive,
      @Param("sort") String sort,
      @Param("direction") String direction);

  Page<User> findAll(Pageable pageable);

  @Query("""
          SELECT DISTINCT u
          FROM User u
          JOIN u.account a
          JOIN a.accountRoles ar
          JOIN ar.role r
          WHERE r.roleCode <> com.ra.base_spring_boot.model.constants.RoleName.ROLE_ADMIN
            AND (:email IS NULL OR LOWER(a.email) LIKE LOWER(CONCAT('%', :email, '%')))
            AND (:isActive IS NULL OR a.isActive = :isActive)
            AND (:role IS NULL OR r.roleCode = :role)
      """)
  Page<User> searchMembersExcludeAdmin(
      @Param("email") String email,
      @Param("isActive") Boolean isActive,
      @Param("role") com.ra.base_spring_boot.model.constants.RoleName role,
      Pageable pageable);

  @Query("SELECT u FROM User u WHERE u.account.accountId = :accountId")
  Optional<User> findByAccountId(Integer accountId);
}
