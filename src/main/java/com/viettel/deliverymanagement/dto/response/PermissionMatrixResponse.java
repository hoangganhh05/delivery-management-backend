package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PermissionMatrixResponse {
    private List<Role> roles;
    private List<PermissionRowResponse> permissions;
}
