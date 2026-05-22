package com.emms.backend.service.ai;

import com.emms.backend.dto.ai.RiskAssetDTO;
import com.emms.backend.entity.Asset;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RiskEngine {

    public RiskAssetDTO calculate(
            Asset asset,
            Map<Long, Long> workOrderMap,
            Map<Long, Long> downtimeMap
    ) {
        Long assetId = asset.getId();

        long workOrders = workOrderMap.getOrDefault(assetId, 0L);
        long downtime = downtimeMap.getOrDefault(assetId, 0L);

        int severity = calculateSeverity(asset);
        int occurrence = calculateOccurrence(workOrders);
        int downtimeScore = calculateDowntime(downtime);
        int criticality = calculateCriticality(asset);

        int score = (int) Math.round(
                severity * 0.40
                        + occurrence * 0.30
                        + downtimeScore * 0.20
                        + criticality * 0.10
        );

        score = Math.max(0, Math.min(100, score));

        return new RiskAssetDTO(
                asset.getId(),
                asset.getName(),
                score,
                riskLevel(score),
                workOrders,
                downtime,
                asset.getStatus() != null ? asset.getStatus().name() : "UNKNOWN"
        );
    }

    private int calculateSeverity(Asset asset) {
        if (asset.getStatus() == null) {
            return 30;
        }

        return switch (asset.getStatus().name()) {
            case "EMERGENCY_SHUTDOWN" -> 100;
            case "DOWN" -> 90;
            case "MAINTENANCE" -> 70;
            case "INACTIVE" -> 50;
            case "ACTIVE" -> 20;
            default -> 30;
        };
    }

    private int calculateOccurrence(long workOrders) {
        return Math.min((int) workOrders * 10, 100);
    }

    private int calculateDowntime(long downtime) {
        return Math.min((int) downtime * 12, 100);
    }

    private int calculateCriticality(Asset asset) {
        Integer criticality = asset.getCriticality();
        return criticality != null ? criticality : 50;
    }

    private String riskLevel(int score) {
        if (score >= 85) {
            return "CRITICAL";
        }

        if (score >= 65) {
            return "HIGH";
        }

        if (score >= 40) {
            return "MEDIUM";
        }

        return "LOW";
    }
}