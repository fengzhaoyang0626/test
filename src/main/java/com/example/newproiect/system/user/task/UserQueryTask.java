package com.example.newproiect.system.user.task;

import com.example.newproiect.common.exception.BusinessException;
import com.example.newproiect.common.exception.ErrorCode;
import com.example.newproiect.system.user.dto.BatchUserQueryResult;
import com.example.newproiect.system.user.dto.UserResponse;
import com.example.newproiect.system.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 这是一个可以被线程池执行的任务。
 *
 * Callable 和 Runnable 类似，但 Callable 可以返回结果。
 * 这里的 call 方法由线程池中的工作线程执行。
 */
public class UserQueryTask implements Callable<BatchUserQueryResult> {

    private static final Logger log = LoggerFactory.getLogger(UserQueryTask.class);

    private final UserService userService;
    private final Long userId;

    public UserQueryTask(UserService userService, Long userId) {
        this.userService = userService;
        this.userId = userId;
    }

    @Override
    public BatchUserQueryResult call() {
        try {
            // 查询会先检查 Redis，Redis 没有时再查询 MySQL。
            UserResponse user = userService.getById(userId);
            return BatchUserQueryResult.success(userId, user);
        } catch (BusinessException exception) {
            // 例如 USER_NOT_FOUND，只标记当前用户失败。
            ErrorCode errorCode = exception.getErrorCode();
            return BatchUserQueryResult.failure(
                    userId,
                    errorCode.code(),
                    exception.getMessage()
            );
        } catch (RuntimeException exception) {
            // 未知异常也只影响当前用户。
            log.error("Failed to query user {}", userId, exception);
            return BatchUserQueryResult.failure(
                    userId,
                    ErrorCode.INTERNAL_ERROR.code(),
                    ErrorCode.INTERNAL_ERROR.message()
            );
        }
    }
}
