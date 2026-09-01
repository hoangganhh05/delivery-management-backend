package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.Gender;
import com.viettel.deliverymanagement.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMeResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String avatarUrl;
    private Role role;
    private String status;
    private List<UserAddressResponse> addresses;
    private UserSettingsResponse settings;
}
