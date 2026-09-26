package com.viettel.deliverymanagement.repository;

import com.viettel.deliverymanagement.entity.VoucherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<VoucherEntity, Long> {

    Optional<VoucherEntity> findByCode(String code);

    List<VoucherEntity> findByActiveTrueOrderByCreatedAtDesc();

    /** Atomically consumes one limited voucher use and prevents concurrent oversubscription. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update VoucherEntity voucher set voucher.usageLimit = voucher.usageLimit - 1 "
            + "where voucher.id = :id and voucher.usageLimit > 0")
    int consumeOneUse(@Param("id") Long id);
}
