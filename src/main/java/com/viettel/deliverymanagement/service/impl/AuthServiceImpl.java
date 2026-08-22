package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.dto.request.LoginRequest;
import com.viettel.deliverymanagement.dto.request.RegisterRequest;
import com.viettel.deliverymanagement.dto.response.AuthResponse;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.security.JwtTokenProvider;
import com.viettel.deliverymanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Bắt đầu đăng ký tài khoản cho username: {}", request.getUsername());

        // 1. Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
            log.warn("Đăng ký thất bại: Username {} đã tồn tại", request.getUsername());
            throw new AppException("USERNAME_ALREADY_EXISTS", "Tên đăng nhập đã tồn tại trong hệ thống");
        }

        // 2. Kiểm tra số điện thoại đã tồn tại chưa
        if (userRepository.existsByPhoneNumberAndIsDeletedFalse(request.getPhoneNumber())) {
            log.warn("Đăng ký thất bại: Số điện thoại {} đã tồn tại", request.getPhoneNumber());
            throw new AppException("PHONE_ALREADY_EXISTS", "Số điện thoại đã được đăng ký tài khoản khác");
        }

        // 3. Mặc định role là CUSTOMER nếu không truyền
        Role role = request.getRole() != null ? request.getRole() : Role.CUSTOMER;

        // 4. Tạo UserEntity mới với mật khẩu mã hóa BCrypt
        UserEntity newUser = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(role)
                .status("ACTIVE")
                .build();

        UserEntity savedUser = userRepository.save(newUser);
        log.info("Đăng ký tài khoản thành công cho user ID: {}", savedUser.getId());

        // 5. Sinh JWT Token
        String token = jwtTokenProvider.generateToken(
                savedUser.getUsername(),
                savedUser.getFullName(),
                savedUser.getRole()
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(savedUser.getUsername())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Bắt đầu xác thực đăng nhập cho username: {}", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            log.warn("Đăng nhập thất bại cho username: {}. Sai mật khẩu hoặc tài khoản", request.getUsername());
            throw new AppException("INVALID_CREDENTIALS", "Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        UserEntity user = userRepository.findByUsernameAndIsDeletedFalse(request.getUsername())
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy thông tin tài khoản"));

        if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
            log.warn("Tài khoản {} đang bị khóa", user.getUsername());
            throw new AppException("USER_BLOCKED", "Tài khoản của bạn đã bị khóa");
        }

        String token = jwtTokenProvider.generateToken(
                user.getUsername(),
                user.getFullName(),
                user.getRole()
        );

        log.info("Đăng nhập thành công cho username: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}
