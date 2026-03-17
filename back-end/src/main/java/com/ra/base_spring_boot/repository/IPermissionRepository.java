package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPermissionRepository extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByPermissionCode(String permissionCode);
}