package com.example.newproiect.system.user.dto;

public record BatchUserQueryResult(
        Long id,
        boolean success,
        UserResponse user,
        String errorCode,
        String message
) {

    public static BatchUserQueryResult success(Long id, UserResponse user) {
        return new BatchUserQueryResult(id, true, user, null, null);
    }

    public static BatchUserQueryResult failure(Long id, String errorCode, String message) {
        return new BatchUserQueryResult(id, false, null, errorCode, message);
    }
}
