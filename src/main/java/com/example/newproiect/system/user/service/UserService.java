package com.example.newproiect.system.user.service;

import com.example.newproiect.common.api.PageResponse;
import com.example.newproiect.system.user.dto.CreateUserRequest;
import com.example.newproiect.system.user.dto.UpdateUserRequest;
import com.example.newproiect.system.user.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserResponse getById(Long id);

    List<UserResponse> queryList(String name);

    PageResponse<UserResponse> page(int page, int size, String keyword, Integer status);

    UserResponse update(Long id, UpdateUserRequest request);

    void delete(Long id);
}
