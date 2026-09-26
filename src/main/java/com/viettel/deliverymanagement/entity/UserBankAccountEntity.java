package com.viettel.deliverymanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(
        name = "user_bank_accounts",
        indexes = @Index(name = "idx_user_bank_accounts_user_id", columnList = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBankAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "bank_code", length = 30)
    private String bankCode;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "account_holder_name", nullable = false, length = 100)
    private String accountHolderName;

    @Column(name = "account_number_encrypted", nullable = false, length = 512)
    private String accountNumberEncrypted;

    @Column(name = "account_number_last4", nullable = false, length = 4)
    private String accountNumberLast4;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean defaultAccount = false;

    /** True only after a future regulated verification flow has verified it. */
    @Column(name = "verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
