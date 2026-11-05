package com.example.newproiect.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.newproiect.common.api.PageResponse;
import com.example.newproiect.common.exception.BusinessException;
import com.example.newproiect.common.exception.ErrorCode;
import com.example.newproiect.system.user.dto.CreateUserRequest;
import com.example.newproiect.system.user.dto.UpdateUserRequest;
import com.example.newproiect.system.user.dto.UserResponse;
import com.example.newproiect.system.user.entity.UserEntity;
import com.example.newproiect.system.user.mapper.UserMapper;
import com.example.newproiect.system.user.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String username = request.username().trim();
        ensureUsernameAvailable(username);

        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode(request.password()));
        entity.setDisplayName(request.displayName().trim());
        entity.setEmail(normalizeEmail(request.email()));
        entity.setPhone(normalizeOptional(request.phone()));
        entity.setStatus(request.status());
        entity.setDeleted(0);
        userMapper.insert(entity);
        return toResponse(entity);
    }

    @Override
    public UserResponse getById(Long id) {
        return toResponse(requireUser(id));
    }

    @Override
    public List<UserResponse> queryList(Long id) {
        QueryWrapper<UserEntity> queryList = new QueryWrapper<>();
        List<UserResponse> userResponses =new ArrayList<>(); ;
        queryList.eq("id",id);
        List<UserEntity> userEntity=userMapper.selectList(queryList);
        for (UserEntity user:
                userEntity) {
            UserResponse response = toResponse(user);
            userResponses.add(response);
        }
        return userResponses;
    }

    @Override
    public PageResponse<UserResponse> page(int page, int size, String keyword, Integer status) {
        LambdaQueryWrapper<UserEntity> query = new LambdaQueryWrapper<>();
        String normalizedKeyword = normalizeOptional(keyword);
        if (normalizedKeyword != null) {
            query.and(wrapper -> wrapper
                    .like(UserEntity::getUsername, normalizedKeyword)
                    .or()
                    .like(UserEntity::getDisplayName, normalizedKeyword)
                    .or()
                    .like(UserEntity::getEmail, normalizedKeyword));
        }
        query.eq(status != null, UserEntity::getStatus, status)
                .orderByDesc(UserEntity::getCreatedAt)
                .orderByDesc(UserEntity::getId);

        Page<UserEntity> entityPage = userMapper.selectPage(new Page<>(page, size), query);
        List<UserResponse> records = entityPage.getRecords().stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(
                records,
                entityPage.getTotal(),
                entityPage.getCurrent(),
                entityPage.getSize(),
                entityPage.getPages()
        );
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        UserEntity entity = requireUser(id);
        entity.setDisplayName(request.displayName().trim());
        entity.setEmail(normalizeEmail(request.email()));
        entity.setPhone(normalizeOptional(request.phone()));
        entity.setStatus(request.status());
        userMapper.updateById(entity);
        return toResponse(requireUser(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireUser(id);
        userMapper.deleteById(id);
    }

    private void ensureUsernameAvailable(String username) {
        boolean exists = userMapper.exists(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username)
        );
        if (exists) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
    }

    private UserEntity requireUser(Long id) {
        UserEntity entity = userMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return entity;
    }

    private String normalizeEmail(String email) {
        String normalized = normalizeOptional(email);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private UserResponse toResponse(UserEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
