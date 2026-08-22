package com.viettel.deliverymanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @Schema(description = "Tên đăng nhập", example = "admin_viettel")
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @Schema(description = "Mật khẩu tài khoản", example = "Admin@123456")
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
