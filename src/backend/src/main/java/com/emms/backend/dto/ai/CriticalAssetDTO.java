package com.emms.backend.dto.ai;


import java.util.List;

public record CriticalAssetDTO(

        String name,

        Integer riskScore,

        String riskLevel,

        List<String> reason,

        List<String> actions

) {
}