package com.viettel.deliverymanagement.entity;

import jakarta.persistence.*;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "createdAt",updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "createBy",length = 50)
    private String createBy;

    @UpdateTimestamp
    @Column(name = "updateAt", updatable = false)
    private LocalDateTime updateAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false; // X?a m?m (Soft Delete)
}
