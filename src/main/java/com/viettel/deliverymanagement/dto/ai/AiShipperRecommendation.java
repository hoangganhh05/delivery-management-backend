package com.viettel.deliverymanagement.dto.ai;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiShipperRecommendation {

    private Long shipperId;
    private String username;
    private String fullName;
    private String phoneNumber;
    private int matchScore; // 0 - 100
    private Double estimatedDistanceKm;
    private int activeOrders;
    private List<String> reasons;
    private boolean recommended;
}
