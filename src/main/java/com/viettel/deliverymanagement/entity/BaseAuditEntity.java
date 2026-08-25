package com.viettel.deliverymanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * BaseAuditEntity: Lớp cơ sở cung cấp ID và các trường Audit (thời gian, người tạo/cập nhật)
 * Dành cho các Entity không sử dụng tính năng Soft Delete (ví dụ: UserEntity).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseAuditEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}

