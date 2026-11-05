package com.example.newproiect.system.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在 3 到 50 个字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 72, message = "密码长度必须在 8 到 72 个字符之间")
        String password,

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
