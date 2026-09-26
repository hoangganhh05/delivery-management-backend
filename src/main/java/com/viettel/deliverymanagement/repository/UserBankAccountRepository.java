package com.viettel.deliverymanagement.repository;

import com.viettel.deliverymanagement.entity.UserBankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBankAccountRepository extends JpaRepository<UserBankAccountEntity, Long> {

    List<UserBankAccountEntity> findAllByUserIdOrderByDefaultAccountDescCreatedAtAsc(Long userId);

    Optional<UserBankAccountEntity> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserBankAccountEntity account set account.defaultAccount = false "
            + "where account.user.id = :userId and account.defaultAccount = true")
    int clearDefaultForUser(@Param("userId") Long userId);
}
