package com.example.newproiect.system.user.service;

import com.example.newproiect.common.config.UserBatchThreadPoolProperties;
import com.example.newproiect.common.exception.BusinessException;
import com.example.newproiect.common.exception.ErrorCode;
import com.example.newproiect.system.user.dto.BatchUserQueryRequest;
import com.example.newproiect.system.user.dto.BatchUserQueryResponse;
import com.example.newproiect.system.user.dto.BatchUserQueryResult;
import com.example.newproiect.system.user.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class BatchUserQueryService {

    private static final Logger log = LoggerFactory.getLogger(BatchUserQueryService.class);

    private final UserService userService;
    private final Executor userBatchExecutor;
    private final UserBatchThreadPoolProperties properties;

    public BatchUserQueryService(
            UserService userService,
            @Qualifier("userBatchExecutor") Executor userBatchExecutor,
            UserBatchThreadPoolProperties properties
    ) {
        this.userService = userService;
        this.userBatchExecutor = userBatchExecutor;
        this.properties = properties;
    }

    public BatchUserQueryResponse query(BatchUserQueryRequest request) {
        List<Long> ids = request.ids().stream().distinct().toList();
        List<CompletableFuture<BatchUserQueryResult>> futures = ids.stream()
                .map(this::queryAsync)
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        List<BatchUserQueryResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        int successCount = (int) results.stream()
                .filter(BatchUserQueryResult::success)
                .count();
        return new BatchUserQueryResponse(
                results,
                successCount,
                results.size() - successCount
        );
    }

    private CompletableFuture<BatchUserQueryResult> queryAsync(Long id) {
        return CompletableFuture.supplyAsync(() -> queryOne(id), userBatchExecutor)
                .completeOnTimeout(
                        BatchUserQueryResult.failure(
                                id,
                                "BATCH_QUERY_TIMEOUT",
                                "用户查询超时"
                        ),
                        properties.taskTimeout().toMillis(),
                        TimeUnit.MILLISECONDS
                )
                .exceptionally(exception -> {
                    log.error("Unexpected error while querying user {}", id, exception);
                    return BatchUserQueryResult.failure(
                            id,
                            ErrorCode.INTERNAL_ERROR.code(),
                            ErrorCode.INTERNAL_ERROR.message()
                    );
                });
    }

    private BatchUserQueryResult queryOne(Long id) {
        try {
            UserResponse user = userService.getById(id);
            return BatchUserQueryResult.success(id, user);
        } catch (BusinessException exception) {
            ErrorCode errorCode = exception.getErrorCode();
            return BatchUserQueryResult.failure(
                    id,
                    errorCode.code(),
                    exception.getMessage()
            );
        } catch (RuntimeException exception) {
            log.error("Failed to query user {}", id, exception);
            return BatchUserQueryResult.failure(
                    id,
                    ErrorCode.INTERNAL_ERROR.code(),
                    ErrorCode.INTERNAL_ERROR.message()
            );
        }
    }
}
