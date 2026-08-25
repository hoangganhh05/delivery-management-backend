package com.viettel.deliverymanagement.entity;

import com.viettel.deliverymanagement.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
