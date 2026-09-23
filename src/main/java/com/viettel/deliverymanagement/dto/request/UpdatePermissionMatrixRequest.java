package com.viettel.deliverymanagement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdatePermissionMatrixRequest {
    @NotNull @Valid private List<PermissionValueRequest> permissions;
}
