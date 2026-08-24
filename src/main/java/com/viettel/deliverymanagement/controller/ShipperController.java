package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.dto.response.ShipperDto;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.repository.ShipmentRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shippers")
@RequiredArgsConstructor
@Tag(name = "Shipper Management", description = "APIs quản lý đội ngũ nhân viên giao hàng")
public class ShipperController {

    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả nhân viên giao hàng (Shipper)")
    public ResponseData<List<ShipperDto>> getAllShippers() {
        List<UserEntity> shippers = userRepository.findByRoleAndIsDeletedFalse(Role.SHIPPER);
        List<ShipperDto> dtoList = shippers.stream().map(s -> ShipperDto.builder()
                .id(s.getId())
                .username(s.getUsername())
                .fullName(s.getFullName())
                .phoneNumber(s.getPhoneNumber())
                .email(s.getEmail())
                .status(s.getStatus() != null ? s.getStatus() : "ACTIVE")
                .activeOrderCount(0L)
                .build()
        ).collect(Collectors.toList());

        return ResponseData.success("Lấy danh sách shipper thành công", dtoList);
    }
}
