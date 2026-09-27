package com.viettel.deliverymanagement.repository;

import com.viettel.deliverymanagement.entity.ShipmentOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShipmentOfferRepository extends JpaRepository<ShipmentOfferEntity, Long> {
    List<ShipmentOfferEntity> findByShipperIdAndStatusOrderByExpiresAtAsc(Long shipperId, String status);
    List<ShipmentOfferEntity> findByStatusAndExpiresAtBefore(String status, LocalDateTime now);
    boolean existsByOrderIdAndShipperIdAndStatus(Long orderId, Long shipperId, String status);
    @Query("select o from ShipmentOfferEntity o where o.id = :id")
    Optional<ShipmentOfferEntity> findByIdForUpdate(@Param("id") Long id);
}
