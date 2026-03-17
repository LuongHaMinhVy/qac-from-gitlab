package com.ra.base_spring_boot.repository.account;

import com.ra.base_spring_boot.model.RolePermission;
import com.ra.base_spring_boot.model.constants.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer> {

    @Query("SELECT rp.permission.permissionCode FROM RolePermission rp WHERE rp.role.roleId IN :roleIds")
    Set<String> findPermissionCodesByRoleIds(@Param("roleIds") List<Integer> roleIds);

    @Query("SELECT rp.permission.permissionCode FROM RolePermission rp WHERE rp.role.roleCode IN :roleCodes")
    Set<String> findPermissionCodesByRoleCodes(@Param("roleCodes") List<RoleName> roleCodes);

    void deleteAllByRole_RoleId(Integer roleId);
}
