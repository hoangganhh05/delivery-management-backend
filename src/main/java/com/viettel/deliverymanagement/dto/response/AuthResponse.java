package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse implements Serializable {

    @Schema(description = "JWT Access Token dùng để xác thực các API sau", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Loại Token", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Tên đăng nhập", example = "admin_viettel")
    private String username;

    @Schema(description = "Họ và tên", example = "Nguyễn Văn Nam")
    private String fullName;

    @Schema(description = "Vai trò người dùng trong hệ thống", example = "ADMIN")
    private Role role;
}
