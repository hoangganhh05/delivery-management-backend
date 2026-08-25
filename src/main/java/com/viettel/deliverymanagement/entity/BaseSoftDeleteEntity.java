package com.viettel.deliverymanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * BaseSoftDeleteEntity: Kế thừa BaseAuditEntity và bổ sung trường cờ xóa mềm (isDeleted)
 * Dành cho các Entity có hỗ trợ Soft Delete (Orders, Shipments, Vouchers, Notifications, ...).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseSoftDeleteEntity extends BaseAuditEntity {

    @Column(name = "is_deleted")
    private Boolean isDeleted = false; // Cờ xóa mềm (Soft delete)
}
