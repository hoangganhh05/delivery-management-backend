package com.viettel.deliverymanagement.repository;

import com.viettel.deliverymanagement.constant.PermissionCode;
import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.entity.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, Long> {

    Optional<RolePermissionEntity> findByRoleAndPermissionCode(Role role, PermissionCode permissionCode);

    List<RolePermissionEntity> findByRoleAndAllowedTrue(Role role);

    List<RolePermissionEntity> findAllByOrderByPermissionCodeAscRoleAsc();
}
