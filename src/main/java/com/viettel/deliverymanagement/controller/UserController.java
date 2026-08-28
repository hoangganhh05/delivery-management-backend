package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.dto.response.UserDto;
import com.viettel.deliverymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseData<List<UserDto>> getUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .phoneNumber(user.getPhoneNumber())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .build())
                .toList();
        return ResponseData.success("Lấy danh sách người dùng thành công", users);
    }
}
