package com.viettel.deliverymanagement.repository;

import com.viettel.deliverymanagement.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {

    List<ShipmentEntity> findByOrderIdOrderByIdDesc(Long orderId);

    boolean existsByOrderIdAndShipperId(Long orderId, Long shipperId);

    Optional<ShipmentEntity> findFirstByOrderIdAndShipperIdIsNotNullOrderByIdDesc(Long orderId);

    @Query("select distinct s.orderId from ShipmentEntity s where s.shipperId = :shipperId")
    List<Long> findDistinctOrderIdsByShipperId(@Param("shipperId") Long shipperId);
}
