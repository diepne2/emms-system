package com.emms.backend.controller;

import com.emms.backend.dto.ai.AiAnalysisRequest;
import com.emms.backend.dto.ai.AiAnalysisResponse;
import com.emms.backend.dto.ai.AiDecisionResponseDTO;
import com.emms.backend.service.ai.AiDecisionUseCase;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173"
})
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiDecisionUseCase useCase;

    public AiController(AiDecisionUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/analysis")
    public AiAnalysisResponse analyze(@RequestBody AiAnalysisRequest request) {
        LocalDateTime to = request.to() != null
                ? request.to()
                : LocalDateTime.now();

        LocalDateTime from = request.from() != null
                ? request.from()
                : to.minusDays(30);

        AiDecisionResponseDTO result = useCase.analyze(
                request.question(),
                from,
                to,
                "CUSTOM"
        );

        return new AiAnalysisResponse(result.summary(), result);
    }

    @GetMapping("/analysis/quick")
    public AiAnalysisResponse quickAnalyze(@RequestParam String question) {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(30);

        AiDecisionResponseDTO result = useCase.analyze(
                question,
                from,
                to,
                "30D"
        );

        return new AiAnalysisResponse(result.summary(), result);
    }

    @GetMapping("/analysis/month")
    public AiAnalysisResponse monthlyAnalyze(
            @RequestParam String question,
            @RequestParam int month,
            @RequestParam int year
    ) {
        LocalDateTime from = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime to = from.plusMonths(1);

        AiDecisionResponseDTO result = useCase.analyze(
                question,
                from,
                to,
                "MONTH_" + month + "_" + year
        );

        return new AiAnalysisResponse(result.summary(), result);
    }

    @GetMapping("/analysis/year")
    public AiAnalysisResponse yearlyAnalyze(
            @RequestParam String question,
            @RequestParam int year
    ) {
        LocalDateTime from = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime to = LocalDate.of(year + 1, 1, 1).atStartOfDay();

        AiDecisionResponseDTO result = useCase.analyze(
                question,
                from,
                to,
                "YEAR_" + year
        );

        return new AiAnalysisResponse(result.summary(), result);
    }
}