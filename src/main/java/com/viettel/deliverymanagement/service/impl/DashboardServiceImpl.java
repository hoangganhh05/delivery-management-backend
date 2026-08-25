package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.dto.response.DashboardResponse;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        log.info("Bắt đầu thu thập dữ liệu thống kê Dashboard Analytics");

        long totalOrders = orderRepository.count();
        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
        BigDecimal totalRevenue = orderRepository.sumTotalFeeByStatus(OrderStatus.DELIVERED);

        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        log.info("Thống kê hoàn tất: totalOrders={}, deliveredOrders={}, cancelledOrders={}, totalRevenue={}",
                totalOrders, deliveredOrders, cancelledOrders, totalRevenue);

        return DashboardResponse.builder()
                .totalOrders(totalOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue)
                .build();
    }
}
