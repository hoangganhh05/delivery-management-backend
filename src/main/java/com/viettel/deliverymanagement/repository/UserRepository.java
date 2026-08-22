package com.viettel.deliverymanagement.repository;

import com.viettel.deliverymanagement.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsernameAndIsDeletedFalse(String username);

    boolean existsByUsernameAndIsDeletedFalse(String username);

    boolean existsByPhoneNumberAndIsDeletedFalse(String phoneNumber);

    boolean existsByEmailAndIsDeletedFalse(String email);
}