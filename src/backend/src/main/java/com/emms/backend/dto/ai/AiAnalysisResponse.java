package com.emms.backend.dto.ai;

public record AiAnalysisResponse(
        String summary,
        AiDecisionResponseDTO data
) {}