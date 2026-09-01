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

    @Schema(description = "Tên đăng nhập (chữ, số, _, @, ., -)", example = "shipper_nam")
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Pattern(
            regexp = "^[a-zA-Z0-9_@.-]{3,50}$",
            message = "Tên đăng nhập phải từ 3 đến 50 ký tự và chỉ chứa chữ, số, dấu gạch dưới (_), @, chấm (.), hoặc gạch ngang (-)"
    )
    @JsonProperty("username")
    @JsonAlias({"username", "userName", "user_name"})
    private String username;

    @Schema(description = "Mật khẩu tài khoản", example = "Password@123")
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 72, message = "Mật khẩu phải có từ 6 đến 72 ký tự")
    @JsonProperty("password")
    private String password;

    @Schema(description = "Họ và tên đầy đủ", example = "Nguyễn Văn Nam")
    @JsonProperty("fullName")
    @JsonAlias({"fullName", "fullname", "full_name", "name"})
    private String fullName;

    @Schema(description = "Địa chỉ email", example = "nam.nguyen@viettel.com.vn")
    @Email(message = "Email không đúng định dạng")
    @JsonProperty("email")
    private String email;

    @Schema(description = "Số điện thoại liên hệ", example = "0988123456")
    @JsonProperty("phoneNumber")
    @JsonAlias({"phone", "phoneNumber", "phone_number"})
    private String phoneNumber;

    @Schema(description = "Vai trò người dùng (ADMIN, SHIPPER, CUSTOMER). Mặc định là CUSTOMER", example = "CUSTOMER")
    @JsonProperty("role")
    @JsonAlias({"role"})
    @Builder.Default
    private Role role = Role.CUSTOMER;

    public String getPhone() {
        return this.phoneNumber;
    }

    public void setPhone(String phone) {
        this.phoneNumber = phone;
    }
}
