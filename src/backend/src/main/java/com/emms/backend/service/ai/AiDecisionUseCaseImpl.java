package com.emms.backend.service.ai;

import com.emms.backend.dto.ai.AiDecisionResponseDTO;
import com.emms.backend.dto.ai.RiskAssetDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.repository.AssetRepository;
import com.emms.backend.service.GroqService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class AiDecisionUseCaseImpl implements AiDecisionUseCase {

    private final AssetRepository assetRepository;
    private final DataCollector dataCollector;
    private final RiskEngine riskEngine;
    private final PromptBuilder promptBuilder;
    private final GroqService groqService;
    private final ObjectMapper objectMapper;

    public AiDecisionUseCaseImpl(
            AssetRepository assetRepository,
            DataCollector dataCollector,
            RiskEngine riskEngine,
            PromptBuilder promptBuilder,
            GroqService groqService,
            ObjectMapper objectMapper
    ) {
        this.assetRepository = assetRepository;
        this.dataCollector = dataCollector;
        this.riskEngine = riskEngine;
        this.promptBuilder = promptBuilder;
        this.groqService = groqService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiDecisionResponseDTO analyze(
            String question,
            LocalDateTime from,
            LocalDateTime to,
            String label
    ) {
        LocalDateTime safeTo = to != null ? to : LocalDateTime.now();
        LocalDateTime safeFrom = from != null ? from : safeTo.minusDays(30);
        String safeLabel = label != null ? label : "CUSTOM";

        List<Asset> assets = assetRepository.findAll();

        int totalAssets = assets.size();

        Map<Long, Long> workOrderMap =
                dataCollector.collectWorkOrders(safeFrom, safeTo);

        Map<Long, Long> downtimeMap =
                dataCollector.collectDowntime(safeFrom, safeTo);

        List<RiskAssetDTO> risks = assets.stream()
                .filter(asset -> asset.getId() != null)
                .map(asset -> riskEngine.calculate(asset, workOrderMap, downtimeMap))
                .sorted(Comparator.comparingInt(RiskAssetDTO::riskScore).reversed())
                .limit(20)
                .toList();

        String prompt = promptBuilder.build(risks, question, safeLabel);

        AiModelResult aiResult = askAiSafely(prompt);

        String summary = isBlank(aiResult.summary())
                ? buildFallbackSummary(totalAssets, risks)
                : aiResult.summary();

        List<String> recommendations = isEmpty(aiResult.recommendations())
                ? buildRecommendations(risks)
                : aiResult.recommendations();

        List<String> priorityActions = isEmpty(aiResult.priorityActions())
                ? buildPriorityActions(risks)
                : aiResult.priorityActions();

        return new AiDecisionResponseDTO(
                summary,
                totalAssets,
                risks,
                recommendations,
                priorityActions
        );
    }

    private AiModelResult askAiSafely(String prompt) {
        try {
            String rawResponse = groqService.ask(prompt);
            String cleanJson = cleanJson(rawResponse);

            return objectMapper.readValue(cleanJson, AiModelResult.class);
        } catch (Exception e) {
            return new AiModelResult(
                    "AI không phản hồi đúng định dạng JSON. Hệ thống đã sử dụng kết quả phân tích rủi ro nội bộ.",
                    List.of(),
                    List.of()
            );
        }
    }

    private String cleanJson(String value) {
        if (value == null) {
            return "{}";
        }

        return value
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private String buildFallbackSummary(
            int totalAssets,
            List<RiskAssetDTO> risks
    ) {
        long critical = risks.stream()
                .filter(r -> "CRITICAL".equals(r.riskLevel()))
                .count();

        long high = risks.stream()
                .filter(r -> "HIGH".equals(r.riskLevel()))
                .count();

        return "Hệ thống hiện có " + totalAssets
                + " thiết bị. AI đang phân tích top " + risks.size()
                + " thiết bị có rủi ro cao nhất. Trong đó có "
                + critical + " thiết bị mức CRITICAL và "
                + high + " thiết bị mức HIGH.";
    }

    private List<String> buildRecommendations(List<RiskAssetDTO> risks) {
        return risks.stream()
                .filter(r -> r.riskScore() >= 65)
                .limit(5)
                .map(r -> "Ưu tiên lập kế hoạch bảo trì cho thiết bị "
                        + r.name()
                        + " vì đang ở mức rủi ro "
                        + r.riskLevel()
                        + " với điểm "
                        + r.riskScore()
                        + ".")
                .toList();
    }

    private List<String> buildPriorityActions(List<RiskAssetDTO> risks) {
        return risks.stream()
                .filter(r -> r.riskScore() >= 85)
                .limit(5)
                .map(r -> "Kiểm tra khẩn cấp thiết bị "
                        + r.name()
                        + ", rà soát tình trạng vận hành, lịch sử Work Order và downtime.")
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isEmpty(List<String> values) {
        return values == null || values.isEmpty();
    }

    private record AiModelResult(
            String summary,
            List<String> recommendations,
            List<String> priorityActions
    ) {
    }
}