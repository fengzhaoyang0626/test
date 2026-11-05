package com.example.newproiect.system.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchUserQueryRequest(
        @NotEmpty(message = "用户 ID 列表不能为空")
        @Size(max = 100, message = "一次最多查询 100 个用户")
        List<@NotNull(message = "用户 ID 不能为空")
                @Positive(message = "用户 ID 必须大于 0") Long> ids
) {
}
