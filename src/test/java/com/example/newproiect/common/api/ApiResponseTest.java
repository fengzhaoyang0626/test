package com.example.newproiect.common.api;

import com.example.newproiect.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        ApiResponse<Map<String, Long>> response = ApiResponse.ok(Map.of("id", 1L));

        assertThat(response.success()).isTrue();
        assertThat(response.code()).isEqualTo("OK");
        assertThat(response.data()).containsEntry("id", 1L);
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void shouldCreateFailureResponse() {
        ApiResponse<Void> response = ApiResponse.failure(ErrorCode.USER_NOT_FOUND);

        assertThat(response.success()).isFalse();
        assertThat(response.code()).isEqualTo("USER_NOT_FOUND");
        assertThat(response.message()).isEqualTo("用户不存在");
        assertThat(response.timestamp()).isNotNull();
    }
}
