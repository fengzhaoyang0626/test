package com.example.newproiect.system.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "显示名称不能为空")
        @Size(max = 80, message = "显示名称不能超过 80 个字符")
        String displayName,

        @Email(message = "邮箱格式不正确")
        @Size(max = 120, message = "邮箱不能超过 120 个字符")
        String email,

        @Pattern(regexp = "^$|^[0-9+\\- ]{6,20}$", message = "手机号格式不正确")
        String phone,

        @NotNull(message = "状态不能为空")
        @Min(value = 0, message = "状态只能是 0 或 1")
        @Max(value = 1, message = "状态只能是 0 或 1")
        Integer status
) {
}
