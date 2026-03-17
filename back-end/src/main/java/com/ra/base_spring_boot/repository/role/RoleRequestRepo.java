package com.ra.base_spring_boot.repository.role;

import com.ra.base_spring_boot.model.RoleRequest;
import com.ra.base_spring_boot.model.constants.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRequestRepo extends JpaRepository<RoleRequest, Long> {
    Optional<RoleRequest> findByAccount_AccountIdAndRequestedRole_RoleCodeAndStatus(
            Integer accountId, 
            com.ra.base_spring_boot.model.constants.RoleName roleCode, 
            ApprovalStatus status
    );
    
    List<RoleRequest> findByStatus(ApprovalStatus status);
    
    List<RoleRequest> findByAccount_AccountId(Integer accountId);
}

