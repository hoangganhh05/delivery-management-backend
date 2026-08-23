package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.request.LoginRequest;
import com.viettel.deliverymanagement.dto.request.RegisterRequest;
import com.viettel.deliverymanagement.dto.response.AuthResponse;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Authentication Controller", description = "APIs Đăng ký, Đăng nhập và Xác thực JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Đăng ký tài khoản mới",
            description = "Tạo tài khoản mới với mật khẩu mã hóa BCrypt, kiểm tra trùng lặp và trả về JWT Access Token"
    )
    public ResponseData<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseData.success("Đăng ký tài khoản thành công", response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Đăng nhập hệ thống",
            description = "Xác thực username/password và cấp phát JWT Access Token"
    )
    public ResponseData<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseData.success("Đăng nhập thành công", response);
    }
}
