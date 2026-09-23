package com.viettel.deliverymanagement.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class OperationsReportResponse {
    private LocalDate from;
    private LocalDate to;
    private long totalOrders;
    private long deliveredOrders;
    private long failedOrders;
    private BigDecimal revenue;
    private Map<String, Long> statusDistribution;
    private List<ReportPointResponse> timeline;
}
