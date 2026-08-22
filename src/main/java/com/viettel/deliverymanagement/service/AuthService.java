package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.request.LoginRequest;
import com.viettel.deliverymanagement.dto.request.RegisterRequest;
import com.viettel.deliverymanagement.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
