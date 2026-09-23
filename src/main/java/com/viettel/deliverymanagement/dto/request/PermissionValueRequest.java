package com.viettel.deliverymanagement.dto.request;

import com.viettel.deliverymanagement.constant.PermissionCode;
import com.viettel.deliverymanagement.constant.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionValueRequest {
    @NotNull private PermissionCode code;
    @NotNull private Role role;
    private boolean allowed;
}
