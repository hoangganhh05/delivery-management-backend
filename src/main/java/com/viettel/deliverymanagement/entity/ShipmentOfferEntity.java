package com.viettel.deliverymanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipment_offers", indexes = {
        @Index(name = "idx_offer_shipper_status", columnList = "shipper_id,status"),
        @Index(name = "idx_offer_order_status", columnList = "order_id,status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentOfferEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_id", nullable = false) private Long orderId;
    @Column(name = "shipper_id", nullable = false) private Long shipperId;
    @Column(nullable = false, length = 20) @Builder.Default private String status = "PENDING";
    @Column(name = "expires_at", nullable = false) private LocalDateTime expiresAt;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "responded_at") private LocalDateTime respondedAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
