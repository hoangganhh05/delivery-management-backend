package com.viettel.deliverymanagement.repository;

import com.viettel.deliverymanagement.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {

    List<ShipmentEntity> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
