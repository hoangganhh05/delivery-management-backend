package com.viettel.deliverymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData<T> implements Serializable {
    private String code;
    private String message;
    private T data;

    // Trả về response thành công
    public static <T> ResponseData<T> success(T data){
        return ResponseData.<T>builder()
                .code("SUCCESS")
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

    // Trả về response thất bại/lỗi không có data
    public static <T> ResponseData<T> error(String message, String code){
        return ResponseData.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    // Trả về response thất bại/lỗi kèm dữ liệu chi tiết (ví dụ danh sách lỗi field)
    public static <T> ResponseData<T> error(String message, String code, T data){
        return ResponseData.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }
}
