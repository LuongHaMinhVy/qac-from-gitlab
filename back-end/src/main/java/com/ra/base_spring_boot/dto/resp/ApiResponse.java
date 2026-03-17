package com.ra.base_spring_boot.dto.resp;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Pagination pagination;
    private List<Object> errors;
    private LocalDateTime timestamp;

    public ApiResponse(T data, String message) {
        this.success = true;
        this.message = message;
        this.data = data;
        this.errors = null;
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(T data, String message, Pagination pagination) {
        this.success = true;
        this.message = message;
        this.data = data;
        this.pagination = pagination;
        this.errors = null;
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(String message, List<Object> errors) {
        this.success = false;
        this.message = message;
        this.data = null;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }

    public static <T> ApiResponse<T> success(T data, String message, Pagination pagination) {
        return new ApiResponse<>(data, message, pagination);
    }

    public static <T> ApiResponse<T> fail(String message, List<Object> errors) {
        return new ApiResponse<>(message, errors);
    }
}
