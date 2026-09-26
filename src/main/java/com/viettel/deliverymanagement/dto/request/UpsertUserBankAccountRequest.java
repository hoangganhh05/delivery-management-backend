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
public class UpsertUserBankAccountRequest {

    @Size(max = 30, message = "Mã ngân hàng không được vượt quá 30 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "Mã ngân hàng chỉ được chứa chữ, số, gạch dưới hoặc gạch ngang")
    private String bankCode;

    @NotBlank(message = "Tên ngân hàng không được để trống")
    @Size(max = 100, message = "Tên ngân hàng không được vượt quá 100 ký tự")
    private String bankName;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    @Size(max = 100, message = "Tên chủ tài khoản không được vượt quá 100 ký tự")
    private String accountHolderName;

    @NotBlank(message = "Số tài khoản không được để trống")
    @Pattern(
            regexp = "^[0-9\\s]{6,40}$",
            message = "Số tài khoản chỉ gồm 6 đến 34 chữ số"
    )
    private String accountNumber;

    private boolean defaultAccount;
}
