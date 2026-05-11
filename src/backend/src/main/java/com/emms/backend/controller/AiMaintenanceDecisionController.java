package com.emms.backend.controller;

import com.emms.backend.dto.ai.AiDecisionRequest;
import com.emms.backend.dto.ai.AiDecisionResponse;
import com.emms.backend.service.AiMaintenanceDecisionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-decision")
public class AiMaintenanceDecisionController {

    private final AiMaintenanceDecisionService aiMaintenanceDecisionService;

    public AiMaintenanceDecisionController(
            AiMaintenanceDecisionService aiMaintenanceDecisionService
    ) {
        this.aiMaintenanceDecisionService = aiMaintenanceDecisionService;
    }

    @PostMapping("/ask")
    public AiDecisionResponse ask(@RequestBody AiDecisionRequest request) {
        return aiMaintenanceDecisionService.ask(request.getQuestion());
    }
}