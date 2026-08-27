package com.viettel.deliverymanagement.entity;

import com.viettel.deliverymanagement.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Column(name = "sender_phone", length = 20)
    private String senderPhone;

    @Column(name = "sender_address", columnDefinition = "TEXT")
    private String senderAddress;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(name = "receiver_address", columnDefinition = "TEXT")
    private String receiverAddress;

    @Column(name = "weight_gram")
    private Integer weightGram;

    @Column(name = "shipping_fee", precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "discount_fee", precision = 12, scale = 2)
    private BigDecimal discountFee;

    @Column(name = "total_fee", precision = 12, scale = 2)
    private BigDecimal totalFee;

    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "cod_amount", precision = 12, scale = 2)
    private BigDecimal codAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, columnDefinition = "VARCHAR(30)")
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items;
}
