package com.viettel.deliverymanagement.dto.request;

import com.viettel.deliverymanagement.constant.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^[0-9+().\\s-]{8,20}$",
            message = "Số điện thoại phải có từ 8 đến 20 ký tự hợp lệ"
    )
    private String phoneNumber;

    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 1024, message = "Đường dẫn ảnh đại diện không được vượt quá 1024 ký tự")
    @Pattern(
            regexp = "^(?:\\s*|https?://.+)$",
            message = "Ảnh đại diện phải là đường dẫn http hoặc https hợp lệ"
    )
    private String avatarUrl;
}
