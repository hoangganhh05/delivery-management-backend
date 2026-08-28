package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String email;
    private Role role;
    private String status;
}
