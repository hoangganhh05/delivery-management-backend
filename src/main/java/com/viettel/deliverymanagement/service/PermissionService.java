package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.constant.PermissionCode;
import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.dto.request.UpdatePermissionMatrixRequest;
import com.viettel.deliverymanagement.dto.response.CurrentPermissionsResponse;
import com.viettel.deliverymanagement.dto.response.PermissionMatrixResponse;
import com.viettel.deliverymanagement.dto.response.PermissionRowResponse;
import com.viettel.deliverymanagement.entity.RolePermissionEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.RolePermissionRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("permissionService")
@RequiredArgsConstructor
public class PermissionService {

    private final RolePermissionRepository repository;
    private final UserRepository userRepository;

    @Transactional
    public void seedDefaults() {
        for (Role role : Role.values()) {
            for (PermissionCode code : PermissionCode.values()) {
                if (repository.findByRoleAndPermissionCode(role, code).isEmpty()) {
                    repository.save(RolePermissionEntity.builder()
                            .role(role).permissionCode(code).allowed(defaultAllowed(role, code)).build());
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean has(Authentication authentication, String codeValue) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        try {
            Role role = currentUser(authentication.getName()).getRole();
            if (role == Role.ADMIN) return true;
            PermissionCode code = PermissionCode.valueOf(codeValue);
            return repository.findByRoleAndPermissionCode(role, code)
                    .map(RolePermissionEntity::isAllowed)
                    .orElseGet(() -> defaultAllowed(role, code));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public CurrentPermissionsResponse current(String username) {
        Role role = currentUser(username).getRole();
        Set<PermissionCode> permissions = EnumSet.noneOf(PermissionCode.class);
        for (PermissionCode code : PermissionCode.values()) {
            boolean allowed = role == Role.ADMIN || repository.findByRoleAndPermissionCode(role, code)
                    .map(RolePermissionEntity::isAllowed)
                    .orElseGet(() -> defaultAllowed(role, code));
            if (allowed) permissions.add(code);
        }
        return CurrentPermissionsResponse.builder().role(role).permissions(permissions).build();
    }

    @Transactional(readOnly = true)
    public PermissionMatrixResponse matrix() {
        List<PermissionRowResponse> rows = Arrays.stream(PermissionCode.values()).map(code -> {
            Map<Role, Boolean> values = new EnumMap<>(Role.class);
            for (Role role : Role.values()) {
                values.put(role, role == Role.ADMIN || repository.findByRoleAndPermissionCode(role, code)
                        .map(RolePermissionEntity::isAllowed)
                        .orElseGet(() -> defaultAllowed(role, code)));
            }
            return PermissionRowResponse.builder().code(code).group(code.getGroup())
                    .label(code.getLabel()).roles(values).build();
        }).toList();
        return PermissionMatrixResponse.builder().roles(List.of(Role.values())).permissions(rows).build();
    }

    @Transactional
    public PermissionMatrixResponse update(UpdatePermissionMatrixRequest request) {
        request.getPermissions().forEach(value -> {
            boolean allowed = value.getRole() == Role.ADMIN || value.isAllowed();
            RolePermissionEntity entity = repository
                    .findByRoleAndPermissionCode(value.getRole(), value.getCode())
                    .orElseGet(() -> RolePermissionEntity.builder()
                            .role(value.getRole()).permissionCode(value.getCode()).build());
            entity.setAllowed(allowed);
            repository.save(entity);
        });
        return matrix();
    }

    private UserEntity currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy tài khoản"));
    }

    private boolean defaultAllowed(Role role, PermissionCode code) {
        if (role == Role.ADMIN) return true;
        Set<PermissionCode> allowed = switch (role) {
            case STAFF -> EnumSet.of(PermissionCode.VIEW_ORDERS, PermissionCode.CREATE_ORDER,
                    PermissionCode.EDIT_ORDER, PermissionCode.CANCEL_ORDER, PermissionCode.ASSIGN_SHIPPER,
                    PermissionCode.VIEW_SHIPPERS, PermissionCode.MANAGE_SHIPPERS, PermissionCode.UPDATE_DELIVERY,
                    PermissionCode.VIEW_PAYMENTS, PermissionCode.MANAGE_VOUCHERS, PermissionCode.APPLY_VOUCHER,
                    PermissionCode.VIEW_REPORTS, PermissionCode.EXPORT_DATA, PermissionCode.VIEW_NOTIFICATIONS);
            case SHIPPER -> EnumSet.of(PermissionCode.VIEW_ORDERS, PermissionCode.UPDATE_DELIVERY,
                    PermissionCode.VIEW_NOTIFICATIONS);
            case CUSTOMER -> EnumSet.of(PermissionCode.VIEW_ORDERS, PermissionCode.CREATE_ORDER,
                    PermissionCode.CANCEL_ORDER, PermissionCode.VIEW_PAYMENTS, PermissionCode.APPLY_VOUCHER,
                    PermissionCode.VIEW_NOTIFICATIONS);
            default -> EnumSet.noneOf(PermissionCode.class);
        };
        return allowed.contains(code);
    }
}
