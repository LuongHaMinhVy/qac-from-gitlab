package com.ra.base_spring_boot.repository.account;

import com.ra.base_spring_boot.model.Role;
import com.ra.base_spring_boot.model.constants.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleCode(RoleName roleCode);

    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleCode(RoleName roleCode);
}