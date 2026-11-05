package com.example.newproiect.system.user.service;

import com.example.newproiect.common.config.UserBatchThreadPoolProperties;
import com.example.newproiect.common.exception.ErrorCode;
import com.example.newproiect.system.user.dto.BatchUserQueryRequest;
import com.example.newproiect.system.user.dto.BatchUserQueryResponse;
import com.example.newproiect.system.user.dto.BatchUserQueryResult;
import com.example.newproiect.system.user.task.UserQueryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 批量查询用户服务。
 *
 * 执行顺序：
 * 1. 找出不重复的用户 ID；
 * 2. 每个 ID 创建一个 UserQueryTask；
 * 3. 把任务提交给 userBatchExecutor 线程池；
 * 4. 主线程等待每个任务的结果；
 * 5. 汇总成功和失败数量。
 */
@Service
public class BatchUserQueryService {

    private static final Logger log = LoggerFactory.getLogger(BatchUserQueryService.class);

    private final UserService userService;
    private final ThreadPoolTaskExecutor userBatchExecutor;
    private final UserBatchThreadPoolProperties properties;

    public BatchUserQueryService(
            UserService userService,
            @Qualifier("userBatchExecutor") ThreadPoolTaskExecutor userBatchExecutor,
            UserBatchThreadPoolProperties properties
    ) {
        this.userService = userService;
        this.userBatchExecutor = userBatchExecutor;
        this.properties = properties;
    }

    public BatchUserQueryResponse query(BatchUserQueryRequest request) {
        // 第一步：去掉重复 ID，例如 [1, 1, 2] 会变成 [1, 2]。
        List<Long> ids = removeDuplicateIds(request.ids());

        // 第二步：把每个 ID 包装成一个任务，并提交到线程池。
        List<Future<BatchUserQueryResult>> futures = submitTasks(ids);

        // 第三步：等待任务完成，收集每一个任务的结果。
        List<BatchUserQueryResult> results = waitForResults(ids, futures);

        // 第四步：统计成功和失败数量。
        int successCount = countSuccess(results);
        return new BatchUserQueryResponse(
                results,
                successCount,
                results.size() - successCount
        );
    }

    /**
     * LinkedHashSet 会去重，同时保留请求中 ID 的原始顺序。
     */
    private List<Long> removeDuplicateIds(List<Long> requestIds) {
        Set<Long> uniqueIds = new LinkedHashSet<>(requestIds);
        return new ArrayList<>(uniqueIds);
    }

    /**
     * 每个 ID 对应一个 UserQueryTask。
     * submit 会把任务交给线程池，当前线程不需要自己执行查询。
     */
    private List<Future<BatchUserQueryResult>> submitTasks(List<Long> ids) {
        List<Future<BatchUserQueryResult>> futures = new ArrayList<>();
        for (Long id : ids) {
            UserQueryTask task = new UserQueryTask(userService, id);
            Future<BatchUserQueryResult> future = userBatchExecutor.submit(task);
            futures.add(future);
        }
        return futures;
    }

    /**
     * Future 表示“以后才会拿到的结果”。
     * get 会等待线程执行结束；超过等待时间就返回一个超时结果。
     */
    private List<BatchUserQueryResult> waitForResults(
            List<Long> ids,
            List<Future<BatchUserQueryResult>> futures
    ) {
        List<BatchUserQueryResult> results = new ArrayList<>();
        long timeoutMillis = properties.taskTimeout().toMillis();

        for (int index = 0; index < futures.size(); index++) {
            Long id = ids.get(index);
            Future<BatchUserQueryResult> future = futures.get(index);
            BatchUserQueryResult result = waitForOne(id, future, timeoutMillis);
            results.add(result);
        }
        return results;
    }

    private BatchUserQueryResult waitForOne(
            Long id,
            Future<BatchUserQueryResult> future,
            long timeoutMillis
    ) {
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            // cancel(true) 会向工作线程发送中断请求，但数据库驱动不一定立即响应中断。
            future.cancel(true);
            log.warn("Timed out while querying user {}", id);
            return BatchUserQueryResult.failure(
                    id,
                    "BATCH_QUERY_TIMEOUT",
                    "用户查询超时"
            );
        } catch (InterruptedException exception) {
            // 恢复中断标记，让上层代码知道当前线程曾被中断。
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for user {}", id);
            return BatchUserQueryResult.failure(
                    id,
                    "BATCH_QUERY_INTERRUPTED",
                    "用户查询被中断"
            );
        } catch (ExecutionException exception) {
            log.error("Unexpected error while querying user {}", id, exception);
            return BatchUserQueryResult.failure(
                    id,
                    ErrorCode.INTERNAL_ERROR.code(),
                    ErrorCode.INTERNAL_ERROR.message()
            );
        }
    }

    private int countSuccess(List<BatchUserQueryResult> results) {
        int successCount = 0;
        for (BatchUserQueryResult result : results) {
            if (result.success()) {
                successCount++;
            }
        }
        return successCount;
    }
}
