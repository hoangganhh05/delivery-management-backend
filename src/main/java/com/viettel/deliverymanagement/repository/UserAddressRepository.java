package com.viettel.deliverymanagement.repository;

import com.viettel.deliverymanagement.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {

    List<UserAddressEntity> findAllByUserIdOrderByDefaultAddressDescCreatedAtAsc(Long userId);

    Optional<UserAddressEntity> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserAddressEntity address set address.defaultAddress = false "
            + "where address.user.id = :userId and address.defaultAddress = true")
    int clearDefaultForUser(@Param("userId") Long userId);
}
