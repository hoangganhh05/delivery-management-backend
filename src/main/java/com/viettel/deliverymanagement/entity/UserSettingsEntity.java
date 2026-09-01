package com.viettel.deliverymanagement.entity;

import com.viettel.deliverymanagement.constant.Theme;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "email_notifications", nullable = false)
    @Builder.Default
    private boolean emailNotifications = true;

    @Column(name = "sms_notifications", nullable = false)
    @Builder.Default
    private boolean smsNotifications = false;

    @Column(name = "push_notifications", nullable = false)
    @Builder.Default
    private boolean pushNotifications = true;

    @Column(name = "new_order_notifications", nullable = false)
    @Builder.Default
    private boolean newOrderNotifications = true;

    @Column(name = "status_change_notifications", nullable = false)
    @Builder.Default
    private boolean statusChangeNotifications = true;

    @Column(name = "payment_success_notifications", nullable = false)
    @Builder.Default
    private boolean paymentSuccessNotifications = true;

    @Column(name = "delivery_complete_notifications", nullable = false)
    @Builder.Default
    private boolean deliveryCompleteNotifications = true;

    @Column(name = "shipper_assignment_notifications", nullable = false)
    @Builder.Default
    private boolean shipperAssignmentNotifications = false;

    @Column(name = "service_alert_notifications", nullable = false)
    @Builder.Default
    private boolean serviceAlertNotifications = true;

    @Column(name = "language", nullable = false, length = 10)
    @Builder.Default
    private String language = "vi";

    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Builder.Default
    private Theme theme = Theme.LIGHT;

    @Column(name = "accent_color", nullable = false, length = 7)
    @Builder.Default
    private String accentColor = "#2563EB";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserSettingsEntity defaultsFor(UserEntity user) {
        return UserSettingsEntity.builder().user(user).build();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
