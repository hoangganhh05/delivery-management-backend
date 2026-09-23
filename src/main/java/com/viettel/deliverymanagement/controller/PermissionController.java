package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.request.UpdatePermissionMatrixRequest;
import com.viettel.deliverymanagement.dto.response.CurrentPermissionsResponse;
import com.viettel.deliverymanagement.dto.response.PermissionMatrixResponse;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping("/me")
    public ResponseData<CurrentPermissionsResponse> current(Authentication authentication) {
        return ResponseData.success("Lấy quyền hiện tại thành công", permissionService.current(authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @permissionService.has(authentication, 'MANAGE_ROLES')")
    public ResponseData<PermissionMatrixResponse> matrix() {
        return ResponseData.success("Lấy ma trận quyền thành công", permissionService.matrix());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or @permissionService.has(authentication, 'MANAGE_ROLES')")
    public ResponseData<PermissionMatrixResponse> update(@Valid @RequestBody UpdatePermissionMatrixRequest request) {
        return ResponseData.success("Cập nhật phân quyền thành công", permissionService.update(request));
    }
}
