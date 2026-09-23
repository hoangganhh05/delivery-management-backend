package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.PermissionCode;
import com.viettel.deliverymanagement.constant.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class PermissionRowResponse {
    private PermissionCode code;
    private String group;
    private String label;
    private Map<Role, Boolean> roles;
}
