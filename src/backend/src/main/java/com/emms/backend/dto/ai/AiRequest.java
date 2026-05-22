package com.emms.backend.dto.ai;
import java.time.LocalDateTime;

public record AiRequest(
        String question,
        LocalDateTime from,
        LocalDateTime to
) {}