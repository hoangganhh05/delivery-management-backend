package com.viettel.deliverymanagement.entity;

import com.viettel.deliverymanagement.constant.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentEntity extends BaseSoftDeleteEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "shipper_id")
    private Long shipperId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private OrderStatus status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "proof_image_url", length = 500)
    private String proofImageUrl;
}
