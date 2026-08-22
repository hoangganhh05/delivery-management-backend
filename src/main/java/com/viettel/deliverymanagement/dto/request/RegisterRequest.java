package com.viettel.deliverymanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.viettel.deliverymanagement.constant.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class RegisterRequest {

    @Schema(description = "Tên đăng nhập (chữ, số, _, @, ., -)", example = "shipper_nam@viettel.vn")
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Pattern(
            regexp = "^[a-zA-Z0-9_@.-]{3,50}$",
            message = "Tên đăng nhập phải từ 3 đến 50 ký tự và chỉ chứa chữ, số, dấu gạch dưới (_), @, chấm (.), hoặc gạch ngang (-)"
    )
    private String username;

    @Schema(description = "Mật khẩu tài khoản", example = "Password@123")
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @Schema(description = "Họ và tên đầy đủ", example = "Nguyễn Văn Nam")
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @Schema(description = "Địa chỉ email", example = "nam.nguyen@viettel.com.vn")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Schema(description = "Số điện thoại liên hệ", example = "0988123456")
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^[0-9+]{9,15}$",
            message = "Số điện thoại không đúng định dạng (từ 9 đến 15 chữ số)"
    )
    @JsonProperty("phoneNumber")
    @JsonAlias({"phone", "phoneNumber"})
    private String phoneNumber;

    @Schema(description = "Vai trò người dùng (ADMIN, SHIPPER, CUSTOMER). Mặc định là CUSTOMER", example = "CUSTOMER")
    @Builder.Default
    private Role role = Role.CUSTOMER;
}
