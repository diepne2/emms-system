package com.emms.backend.dto.ai;

import java.util.List;

public record AiDecisionResponseDTO(

        String summary,

        int totalAssets,

        List<RiskAssetDTO> risks,

        List<String> recommendations,

        List<String> priorityActions

) {
}