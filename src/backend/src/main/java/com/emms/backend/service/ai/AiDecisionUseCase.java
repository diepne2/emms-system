package com.emms.backend.service.ai;

import com.emms.backend.dto.ai.AiDecisionResponseDTO;

import java.time.LocalDateTime;

public interface AiDecisionUseCase {

    AiDecisionResponseDTO analyze(
            String question,
            LocalDateTime from,
            LocalDateTime to,
            String label
    );
}