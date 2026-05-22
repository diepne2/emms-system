package com.emms.backend.dto.ai;

import java.util.List;

public record AiDecisionAIResultDTO(

        String summary,

        List<CriticalAssetDTO> criticalAssets,

        List<String> recommendations,

        List<String> priorityActions

) {
}