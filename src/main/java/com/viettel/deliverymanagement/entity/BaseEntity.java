package com.viettel.deliverymanagement.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * BaseEntity: Alias kế thừa từ BaseSoftDeleteEntity để đảm bảo tương thích
 * cho các Entity đang kế thừa trực tiếp BaseEntity.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity extends BaseSoftDeleteEntity {
}
