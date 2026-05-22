package com.emms.backend.dto.ai;


import java.time.LocalDateTime;

public record AiAnalysisRequest(
        String question,
        LocalDateTime from,
        LocalDateTime to
) {}