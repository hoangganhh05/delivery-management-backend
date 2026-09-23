package com.viettel.deliverymanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ReportPointResponse {
    private LocalDate date;
    private long orders;
    private BigDecimal revenue;
}
