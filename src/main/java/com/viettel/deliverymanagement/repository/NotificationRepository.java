package com.viettel.deliverymanagement.repository;

import com.viettel.deliverymanagement.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalseAndIsDeletedFalse(Long userId);
}
