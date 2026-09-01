package com.viettel.deliverymanagement.dto.request;

import com.viettel.deliverymanagement.constant.Theme;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class UpdateUserSettingsRequest {

    @NotNull(message = "Tùy chọn thông báo email không được để trống")
    private Boolean emailNotifications;

    @NotNull(message = "Tùy chọn thông báo SMS không được để trống")
    private Boolean smsNotifications;

    @NotNull(message = "Tùy chọn push notification không được để trống")
    private Boolean pushNotifications;

    @NotNull(message = "Tùy chọn thông báo đơn mới không được để trống")
    private Boolean newOrderNotifications;

    @NotNull(message = "Tùy chọn thông báo đổi trạng thái không được để trống")
    private Boolean statusChangeNotifications;

    @NotNull(message = "Tùy chọn thông báo thanh toán không được để trống")
    private Boolean paymentSuccessNotifications;

    @NotNull(message = "Tùy chọn thông báo giao hàng không được để trống")
    private Boolean deliveryCompleteNotifications;

    @NotNull(message = "Tùy chọn thông báo phân công shipper không được để trống")
    private Boolean shipperAssignmentNotifications;

    @NotNull(message = "Tùy chọn cảnh báo dịch vụ không được để trống")
    private Boolean serviceAlertNotifications;

    @NotBlank(message = "Ngôn ngữ không được để trống")
    @Pattern(regexp = "(?i)vi|en", message = "Ngôn ngữ chỉ hỗ trợ vi hoặc en")
    private String language;

    @NotNull(message = "Giao diện không được để trống")
    private Theme theme;

    @NotBlank(message = "Màu chủ đạo không được để trống")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Màu chủ đạo phải có định dạng #RRGGBB")
    private String accentColor;
}
