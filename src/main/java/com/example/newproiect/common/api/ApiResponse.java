package com.example.newproiect.common.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.newproiect.common.exception.ErrorCode;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", "请求成功", data, LocalDateTime.now());
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, "OK", "请求成功", null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode) {
        return new ApiResponse<>(
                false,
                errorCode.code(),
                errorCode.message(),
                null,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, T data) {
        return new ApiResponse<>(
                false,
                errorCode.code(),
                errorCode.message(),
                data,
                LocalDateTime.now()
        );
    }
}
