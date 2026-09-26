package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.constant.PaymentStatus;
import com.viettel.deliverymanagement.constant.PaymentMethod;
import com.viettel.deliverymanagement.dto.response.OperationsReportResponse;
import com.viettel.deliverymanagement.dto.response.ReportPointResponse;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public OperationsReportResponse operations(LocalDate from, LocalDate to) {
        DateRange range = normalizeRange(from, to);
        LocalDate start = range.start();
        LocalDate end = range.end();
        List<OrderEntity> orders = filtered(start, end);
        Map<String, Long> statuses = orders.stream().collect(Collectors.groupingBy(
                order -> order.getStatus() == null ? "UNKNOWN" : order.getStatus().name(),
                LinkedHashMap::new, Collectors.counting()));
        Map<LocalDate, List<OrderEntity>> byDate = orders.stream().collect(Collectors.groupingBy(
                order -> order.getCreatedAt().toLocalDate(), LinkedHashMap::new, Collectors.toList()));
        List<ReportPointResponse> timeline = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<OrderEntity> daily = byDate.getOrDefault(date, List.of());
            timeline.add(new ReportPointResponse(date, daily.size(), paidRevenue(daily)));
        }
        return OperationsReportResponse.builder().from(start).to(end).totalOrders(orders.size())
                .deliveredOrders(orders.stream().filter(order -> order.getStatus() != null
                        && order.getStatus().isDeliveryCompleted()).count())
                .failedOrders(orders.stream().filter(order -> order.getStatus() != null
                        && (order.getStatus().name().equals("FAILED")
                        || order.getStatus().name().equals("CANCELLED"))).count())
                .revenue(paidRevenue(orders)).statusDistribution(statuses).timeline(timeline).build();
    }

    @Transactional(readOnly = true)
    public byte[] ordersCsv(LocalDate from, LocalDate to) {
        DateRange range = normalizeRange(from, to);
        LocalDate start = range.start();
        LocalDate end = range.end();
        StringBuilder csv = new StringBuilder("trackingNumber,createdAt,status,paymentMethod,paymentStatus,totalFee\n");
        filtered(start, end).forEach(order -> csv.append(order.getTrackingNumber()).append(',')
                .append(order.getCreatedAt()).append(',').append(order.getStatus()).append(',')
                .append(order.getPaymentMethod()).append(',').append(order.getPaymentStatus()).append(',')
                .append(order.getTotalFee()).append('\n'));
        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
    }

    private List<OrderEntity> filtered(LocalDate from, LocalDate to) {
        return orderRepository.findConfirmedForReport(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay(),
                PaymentMethod.COD,
                PaymentStatus.PAID
        );
    }

    private DateRange normalizeRange(LocalDate from, LocalDate to) {
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.minusDays(29) : from;
        if (start.isAfter(end)) {
            throw new AppException("INVALID_REPORT_RANGE", "Ngày bắt đầu không được sau ngày kết thúc");
        }
        if (start.isBefore(end.minusDays(365))) {
            throw new AppException("REPORT_RANGE_TOO_LARGE", "Khoảng báo cáo không được vượt quá 366 ngày");
        }
        return new DateRange(start, end);
    }

    private BigDecimal paidRevenue(List<OrderEntity> orders) {
        return orders.stream().filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                .map(OrderEntity::getTotalFee).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private record DateRange(LocalDate start, LocalDate end) {}
}
