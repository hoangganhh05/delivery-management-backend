package com.viettel.deliverymanagement.entity;

import com.viettel.deliverymanagement.constant.Role;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * UserEntity: Ánh xạ chuẩn theo schema bảng users thực tế trong Database.
 * Không chứa các trường audit (created_at, updated_at, is_deleted) để tránh lỗi thiếu cột.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private Role role;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, BLOCKED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
