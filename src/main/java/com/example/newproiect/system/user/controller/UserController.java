package com.example.newproiect.system.user.controller;

import com.example.newproiect.common.api.ApiResponse;
import com.example.newproiect.common.api.PageResponse;
import com.example.newproiect.system.user.dto.CreateUserRequest;
import com.example.newproiect.system.user.dto.UpdateUserRequest;
import com.example.newproiect.system.user.dto.UserResponse;
import com.example.newproiect.system.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户的新增、查询、修改和逻辑删除")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "新增用户", description = "密码会使用 BCrypt 加密后保存，接口不会返回密码。")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(userService.create(request)));
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(
            @Parameter(description = "用户 ID", example = "1")
            @PathVariable Long id
    ) {
        return ApiResponse.ok(userService.getById(id));
    }
    @Operation(summary = "查询用户列表", description = "不传 id 时查询全部用户，传入 id 时只返回该用户。")
    @GetMapping("/list")
    public ApiResponse<List<UserResponse>> list(
            @Parameter(description = "可选的用户 ID，不传则查询全部")
            @RequestParam(required = false) @Min(1) Long id
    ) {
        return ApiResponse.ok(userService.queryList(id));
    }

    @Operation(summary = "分页查询用户", description = "keyword 会同时匹配用户名、显示名称和邮箱。")
    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> page(
            @Parameter(description = "页码，从 1 开始")
            @RequestParam(defaultValue = "1") @Min(1) int page,

            @Parameter(description = "每页条数，最大 100")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

            @Parameter(description = "用户名、显示名称或邮箱关键字")
            @RequestParam(required = false) @Size(max = 50) String keyword,

            @Parameter(description = "状态：0 禁用，1 启用")
            @RequestParam(required = false) @Min(0) @Max(1) Integer status
    ) {
        return ApiResponse.ok(userService.page(page, size, keyword, status));
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(
            @Parameter(description = "用户 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ApiResponse.ok(userService.update(id, request));
    }

    @Operation(summary = "删除用户", description = "使用逻辑删除，数据库记录不会被物理删除。")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "用户 ID", example = "1")
            @PathVariable Long id
    ) {
        userService.delete(id);
        return ApiResponse.ok();
    }
}
