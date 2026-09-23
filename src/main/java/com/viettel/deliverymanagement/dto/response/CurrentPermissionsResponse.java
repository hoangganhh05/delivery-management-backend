package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.PermissionCode;
import com.viettel.deliverymanagement.constant.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class CurrentPermissionsResponse {
    private Role role;
    private Set<PermissionCode> permissions;
}
