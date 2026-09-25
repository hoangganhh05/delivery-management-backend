package com.viettel.deliverymanagement.entity;

import com.viettel.deliverymanagement.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

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

    /** Latest GPS point reported by the assigned shipper for this delivery. */
    @Column(name = "current_latitude", precision = 10, scale = 7)
    private java.math.BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 10, scale = 7)
    private java.math.BigDecimal currentLongitude;

    @Column(name = "current_accuracy_m", precision = 10, scale = 2)
    private java.math.BigDecimal currentAccuracyMeters;

    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
