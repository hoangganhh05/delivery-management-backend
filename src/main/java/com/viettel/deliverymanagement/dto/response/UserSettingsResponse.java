package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.Theme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettingsResponse {
    private boolean emailNotifications;
    private boolean smsNotifications;
    private boolean pushNotifications;
    private boolean newOrderNotifications;
    private boolean statusChangeNotifications;
    private boolean paymentSuccessNotifications;
    private boolean deliveryCompleteNotifications;
    private boolean shipperAssignmentNotifications;
    private boolean serviceAlertNotifications;
    private String language;
    private Theme theme;
    private String accentColor;
}
