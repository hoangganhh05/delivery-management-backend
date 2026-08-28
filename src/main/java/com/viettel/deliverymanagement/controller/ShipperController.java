package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.dto.response.ShipperDto;
import com.viettel.deliverymanagement.dto.response.OrderResponse;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.service.OrderService;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả nhân viên giao hàng (Shipper)")
    public ResponseData<List<ShipperDto>> getAllShippers() {
        List<UserEntity> shippers = userRepository.findByRole(Role.SHIPPER);
        List<ShipperDto> dtoList = shippers.stream().map(this::toDto).collect(Collectors.toList());

        return ResponseData.success("Lấy danh sách shipper thành công", dtoList);
    }

    @GetMapping("/{id}")
    public ResponseData<ShipperDto> getShipper(@PathVariable Long id) {
        UserEntity shipper = userRepository.findById(id)
                .filter(user -> user.getRole() == Role.SHIPPER)
                .orElseThrow(() -> new AppException("SHIPPER_NOT_FOUND", "Không tìm thấy shipper"));
        return ResponseData.success("Lấy thông tin shipper thành công", toDto(shipper));
    }

    @GetMapping("/{id}/orders")
    public ResponseData<List<OrderResponse>> getShipperOrders(@PathVariable Long id) {
        return ResponseData.success("Lấy danh sách đơn của shipper thành công", orderService.getOrdersForShipper(id));
    }

    private ShipperDto toDto(UserEntity shipper) {
        long activeOrders = orderService.getOrdersForShipper(shipper.getId()).stream()
                .filter(order -> order.getStatus() != com.viettel.deliverymanagement.constant.OrderStatus.DELIVERED
                        && order.getStatus() != com.viettel.deliverymanagement.constant.OrderStatus.CANCELLED
                        && order.getStatus() != com.viettel.deliverymanagement.constant.OrderStatus.FAILED)
                .count();
        return ShipperDto.builder()
                .id(shipper.getId())
                .username(shipper.getUsername())
                .fullName(shipper.getFullName())
                .phoneNumber(shipper.getPhoneNumber())
                .email(shipper.getEmail())
                .status(shipper.getStatus() != null ? shipper.getStatus() : "ACTIVE")
                .activeOrderCount(activeOrders)
                .build();
    }
}
