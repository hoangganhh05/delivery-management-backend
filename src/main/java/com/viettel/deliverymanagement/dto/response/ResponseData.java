package com.viettel.deliverymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData<T> implements Serializable {
    private String code;
    private String message;
    private T data;

    // Trả về response thành công
    public static <T> ResponseData<T> success(T data){
        return ResponseData.<T>builder()
                .code("success")
                .message("Thao tác thành công")
                .data(data)
                .build();
    }
    // Trả về response thành công kèm message tùy chỉnh
    public static <T> ResponseData<T> success(String message, T data) {
        return ResponseData.<T>builder()
                .code("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }
    // Trả về response thất bại/lỗi
    public static <T> ResponseData<T> error(String message, String code){
        return ResponseData.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
