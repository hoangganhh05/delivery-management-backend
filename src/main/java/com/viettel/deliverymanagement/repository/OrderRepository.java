package com.viettel.deliverymanagement.repository;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.constant.PaymentMethod;
import com.viettel.deliverymanagement.constant.PaymentStatus;
import com.viettel.deliverymanagement.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    Optional<OrderEntity> findByTrackingNumber(String trackingNumber);

    long countByStatus(OrderStatus status);

    long countByStatusIn(Collection<OrderStatus> statuses);

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.paymentMethod = :cod OR o.paymentStatus = :paid")
    long countConfirmedOrders(@Param("cod") PaymentMethod cod, @Param("paid") PaymentStatus paid);

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.status IN :statuses "
            + "AND (o.paymentMethod = :cod OR o.paymentStatus = :paid)")
    long countConfirmedOrdersByStatusIn(@Param("statuses") Collection<OrderStatus> statuses,
            @Param("cod") PaymentMethod cod, @Param("paid") PaymentStatus paid);

    @Query("SELECT COALESCE(SUM(o.totalFee), 0) FROM OrderEntity o WHERE o.status = :status")
    BigDecimal sumTotalFeeByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalFee), 0) FROM OrderEntity o WHERE o.status IN :statuses "
            + "AND (o.paymentMethod = :cod OR o.paymentStatus = :paid)")
    BigDecimal sumTotalFeeByStatusIn(@Param("statuses") Collection<OrderStatus> statuses,
            @Param("cod") PaymentMethod cod, @Param("paid") PaymentStatus paid);

    List<OrderEntity> findByIdInOrderByIdDesc(List<Long> ids);

    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt >= :from AND o.createdAt < :toExclusive "
            + "AND (o.paymentMethod = :cod OR o.paymentStatus = :paid) ORDER BY o.createdAt ASC")
    List<OrderEntity> findConfirmedForReport(
            @Param("from") LocalDateTime from,
            @Param("toExclusive") LocalDateTime toExclusive,
            @Param("cod") PaymentMethod cod,
            @Param("paid") PaymentStatus paid
    );
}
