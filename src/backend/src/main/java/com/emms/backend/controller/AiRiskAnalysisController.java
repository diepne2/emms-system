package com.emms.backend.controller;

import com.emms.backend.dto.ai.AiRiskAssetDTO;
import com.emms.backend.service.AiRiskAnalysisService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-risk")
public class AiRiskAnalysisController {

    private final AiRiskAnalysisService aiRiskAnalysisService;

    public AiRiskAnalysisController(AiRiskAnalysisService aiRiskAnalysisService) {
        this.aiRiskAnalysisService = aiRiskAnalysisService;
    }

    @GetMapping("/assets")
    public List<AiRiskAssetDTO> getAssetRiskAnalysis() {
        return aiRiskAnalysisService.getAssetRiskAnalysis();
    }

    @GetMapping("/assets/{assetId}")
    public AiRiskAssetDTO getAssetRiskAnalysisById(@PathVariable Long assetId) {
        return aiRiskAnalysisService.getAssetRiskAnalysisById(assetId);
    }
}