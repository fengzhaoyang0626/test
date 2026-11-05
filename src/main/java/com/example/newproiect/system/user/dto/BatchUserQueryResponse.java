package com.example.newproiect.system.user.dto;

import java.util.List;

public record BatchUserQueryResponse(
        List<BatchUserQueryResult> results,
        int successCount,
        int failureCount
) {
}
