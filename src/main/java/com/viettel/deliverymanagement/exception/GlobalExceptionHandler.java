package com.viettel.deliverymanagement.exception;

import com.viettel.deliverymanagement.dto.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bắt lỗi do nghiệp vụ tự bắn ra (AppException)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ResponseData<Void>> handleAppException(AppException e) {
        log.warn("Nghiệp vụ AppException [{}]: {}", e.getCode(), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error(e.getMessage(), e.getCode()));
    }

    // Bắt lỗi sai thông tin đăng nhập từ Spring Security (BadCredentialsException)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseData<Void>> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("Lỗi xác thực BadCredentialsException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ResponseData.error("Mật khẩu hoặc tài khoản không chính xác", "UNAUTHORIZED"));
    }

    // Bắt lỗi Validation dữ liệu DTO đầu vào (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        String primaryMessage = errors.values().stream().findFirst().orElse("Dữ liệu đầu vào không hợp lệ");
        log.warn("Lỗi Validation dữ liệu đầu vào: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error(primaryMessage, "INVALID_INPUT", errors));
    }

    // Bắt các lỗi hệ thống không lường trước được (Exception chung)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<Void>> handleGeneralException(Exception e) {
        log.error("Lỗi không mong đợi trong hệ thống: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("Không thể xử lý yêu cầu lúc này. Vui lòng thử lại sau.", "INTERNAL_SERVER_ERROR"));
    }
}
