package com.viettel.deliverymanagement.dto.response;

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
public class ShipperDto {
    private Long id;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String status; // ACTIVE, BUSY, INACTIVE
    private Long activeOrderCount;
}
