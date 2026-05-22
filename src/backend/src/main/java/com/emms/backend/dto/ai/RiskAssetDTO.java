package com.emms.backend.dto.ai;

public record RiskAssetDTO(
        Long id,
        String name,
        int riskScore,
        String riskLevel,
        long workOrders,
        long downtime,
        String status
) {}