package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.dto.response.DashboardResponse;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getDashboardStats_CountsEveryCompletedDeliveryStatus() {
        when(orderRepository.count()).thenReturn(12L);
        when(orderRepository.countByStatusIn(OrderStatus.deliveryCompletedStatuses())).thenReturn(7L);
        when(orderRepository.countByStatus(OrderStatus.CANCELLED)).thenReturn(2L);
        when(orderRepository.sumTotalFeeByStatusIn(OrderStatus.deliveryCompletedStatuses()))
                .thenReturn(BigDecimal.valueOf(560000));

        DashboardResponse response = dashboardService.getDashboardStats();

        assertEquals(12L, response.getTotalOrders());
        assertEquals(7L, response.getDeliveredOrders());
        assertEquals(BigDecimal.valueOf(560000), response.getTotalRevenue());
    }
}
