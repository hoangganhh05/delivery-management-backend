package com.viettel.deliverymanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBankAccountResponse {
    private Long id;
    private String bankCode;
    private String bankName;
    private String accountHolderName;
    private String accountNumberLast4;
    private boolean defaultAccount;
    private boolean verified;
}
