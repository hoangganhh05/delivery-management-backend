package com.viettel.deliverymanagement.exception;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bắt lỗi do nghiệp vụ tự bắn ra (AppException)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ResponseData<Void>> handleAppException(AppException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error(e.getCode(), e.getMessage()));
    }

    // Bắt lỗi Validation dữ liệu DTO đầu vào (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "Dữ liệu không hợp lệ";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseData.error("INVALID_INPUT", errorMessage));
    }

    // Bắt các lỗi hệ thống không lường trước được (Exception chung)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<Void>> handleGeneralException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseData.error("INTERNAL_SERVER_ERROR", "Lỗi hệ thống: " + e.getMessage()));
    }
}