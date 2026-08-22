package com.viettel.deliverymanagement.dto.request;

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

    @Schema(description = "Tên đăng nhập", example = "shipper_nam")
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
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
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không đúng định dạng")
    private String phoneNumber;

    @Schema(description = "Vai trò của tài khoản (ADMIN, SHIPPER, CUSTOMER)", example = "SHIPPER")
    private Role role;
}
