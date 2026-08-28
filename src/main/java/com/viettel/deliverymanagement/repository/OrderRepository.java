package com.viettel.deliverymanagement.repository;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    Optional<OrderEntity> findByTrackingNumber(String trackingNumber);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalFee), 0) FROM OrderEntity o WHERE o.status = :status")
    BigDecimal sumTotalFeeByStatus(@Param("status") OrderStatus status);

    List<OrderEntity> findByIdInOrderByIdDesc(List<Long> ids);
}
