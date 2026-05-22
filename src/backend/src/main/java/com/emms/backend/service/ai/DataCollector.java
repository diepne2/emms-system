package com.emms.backend.service.ai;

import com.emms.backend.repository.AssetDowntimeRepository;
import com.emms.backend.repository.WorkOrderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataCollector {

    private final WorkOrderRepository workOrderRepository;
    private final AssetDowntimeRepository downtimeRepository;

    public DataCollector(
            WorkOrderRepository workOrderRepository,
            AssetDowntimeRepository downtimeRepository
    ) {
        this.workOrderRepository = workOrderRepository;
        this.downtimeRepository = downtimeRepository;
    }

    public Map<Long, Long> collectWorkOrders(
            LocalDateTime from,
            LocalDateTime to
    ) {
        return toMap(workOrderRepository.countByAssetBetweenDates(from, to));
    }

    public Map<Long, Long> collectDowntime(
            LocalDateTime from,
            LocalDateTime to
    ) {
        return toMap(downtimeRepository.countByAssetBetweenDates(from, to));
    }

    private Map<Long, Long> toMap(List<Object[]> rows) {
        Map<Long, Long> result = new HashMap<>();

        if (rows == null) {
            return result;
        }

        for (Object[] row : rows) {
            if (row == null || row.length < 2) {
                continue;
            }

            if (row[0] == null || row[1] == null) {
                continue;
            }

            Long assetId = ((Number) row[0]).longValue();
            Long count = ((Number) row[1]).longValue();

            result.put(assetId, count);
        }

        return result;
    }
}