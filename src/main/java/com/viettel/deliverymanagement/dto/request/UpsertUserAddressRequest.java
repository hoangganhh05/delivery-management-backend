package com.viettel.deliverymanagement.dto.request;

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
public class UpsertUserAddressRequest {

    @NotBlank(message = "Nhãn địa chỉ không được để trống")
    @Size(max = 50, message = "Nhãn địa chỉ không được vượt quá 50 ký tự")
    private String label;

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 100, message = "Tên người nhận không được vượt quá 100 ký tự")
    private String recipientName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    @Pattern(
            regexp = "^[0-9+().\\s-]{8,20}$",
            message = "Số điện thoại phải có từ 8 đến 20 ký tự hợp lệ"
    )
    private String phoneNumber;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự")
    private String addressLine;

    @Size(max = 100, message = "Phường/xã không được vượt quá 100 ký tự")
    private String ward;

    @Size(max = 100, message = "Quận/huyện không được vượt quá 100 ký tự")
    private String district;

    @Size(max = 100, message = "Tỉnh/thành phố không được vượt quá 100 ký tự")
    private String province;

    @Size(max = 20, message = "Mã bưu chính không được vượt quá 20 ký tự")
    private String postalCode;

    private boolean defaultAddress;
}
