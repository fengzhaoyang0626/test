package com.example.newproiect.system.user.cache;

import com.example.newproiect.system.user.dto.UserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
public class UserCacheService {

    private static final Logger log = LoggerFactory.getLogger(UserCacheService.class);
    private static final String USER_KEY_PREFIX = "newproiect:user:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration userTtl;
    private final boolean enabled;

    public UserCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.cache.user-ttl:30m}") Duration userTtl,
            @Value("${app.cache.user-enabled:true}") boolean enabled
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.userTtl = userTtl;
        this.enabled = enabled;
    }

    public Optional<UserResponse> find(Long id) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            String json = redisTemplate.opsForValue().get(key(id));
            if (!StringUtils.hasText(json)) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, UserResponse.class));
        } catch (DataAccessException | JsonProcessingException exception) {
            log.warn("Failed to read user cache for id={}: {}", id, exception.getMessage());
            return Optional.empty();
        }
    }
    public Optional<UserResponse> findname(String name) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            String json = redisTemplate.opsForValue().get(name);
            if (!StringUtils.hasText(json)) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, UserResponse.class));
        } catch (DataAccessException | JsonProcessingException exception) {
            log.warn("Failed to read user cache for id={}: {}", name, exception.getMessage());
            return Optional.empty();
        }
    }
    public void put(UserResponse user) {
        if (!enabled) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key(user.id()), json, userTtl);
        } catch (DataAccessException | JsonProcessingException exception) {
            log.warn("Failed to write user cache for id={}: {}", user.id(), exception.getMessage());
        }
    }
    public void putList(List<UserResponse> list) {
        if (!enabled) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(list);
            redisTemplate.opsForValue().set(("111"), json, userTtl);
        } catch (DataAccessException | JsonProcessingException exception) {
            log.warn("Failed to write user cache for id={}: {}", exception.getMessage());
        }
    }
    public void evict(Long id) {
        if (!enabled) {
            return;
        }
        try {
            redisTemplate.delete(key(id));
        } catch (DataAccessException exception) {
            log.warn("Failed to evict user cache for id={}: {}", id, exception.getMessage());
        }
    }

    private String key(Long id) {
        return USER_KEY_PREFIX + id;
    }
}
