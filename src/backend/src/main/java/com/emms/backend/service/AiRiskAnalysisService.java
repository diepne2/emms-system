package com.emms.backend.service;

import com.emms.backend.dto.ai.AiRiskAssetDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.repository.AssetDowntimeRepository;
import com.emms.backend.repository.AssetRepository;
import com.emms.backend.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AiRiskAnalysisService {

    private final AssetRepository assetRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AssetDowntimeRepository assetDowntimeRepository;
    private final GeminiService geminiService;

    public AiRiskAnalysisService(
            AssetRepository assetRepository,
            WorkOrderRepository workOrderRepository,
            AssetDowntimeRepository assetDowntimeRepository,
            GeminiService geminiService
    ) {
        this.assetRepository = assetRepository;
        this.workOrderRepository = workOrderRepository;
        this.assetDowntimeRepository = assetDowntimeRepository;
        this.geminiService = geminiService;
    }

    public List<AiRiskAssetDTO> getAssetRiskAnalysis() {
        List<Asset> assets = assetRepository.findAll();
        List<AiRiskAssetDTO> result = new ArrayList<>();

        for (Asset asset : assets) {
            Long totalWorkOrders = workOrderRepository.countByAssetId(asset.getId());
            Long totalDowntimes = assetDowntimeRepository.countByAssetId(asset.getId());

            int riskScore = calculateRiskScore(totalWorkOrders, totalDowntimes);
            String riskLevel = getRiskLevel(riskScore);

            String recommendation = generateRecommendation(
                    asset,
                    totalWorkOrders,
                    totalDowntimes,
                    riskScore,
                    riskLevel
            );

            AiRiskAssetDTO dto = new AiRiskAssetDTO(
                    asset.getId(),
                    asset.getName(),
                    String.valueOf(asset.getStatus()),
                    totalWorkOrders,
                    totalDowntimes,
                    riskScore,
                    riskLevel,
                    recommendation
            );

            result.add(dto);
        }

        result.sort(Comparator.comparing(AiRiskAssetDTO::getRiskScore).reversed());

        return result;
    }

    public AiRiskAssetDTO getAssetRiskAnalysisById(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));

        Long totalWorkOrders = workOrderRepository.countByAssetId(asset.getId());
        Long totalDowntimes = assetDowntimeRepository.countByAssetId(asset.getId());

        int riskScore = calculateRiskScore(totalWorkOrders, totalDowntimes);
        String riskLevel = getRiskLevel(riskScore);

        String recommendation = generateRecommendation(
                asset,
                totalWorkOrders,
                totalDowntimes,
                riskScore,
                riskLevel
        );

        return new AiRiskAssetDTO(
                asset.getId(),
                asset.getName(),
                String.valueOf(asset.getStatus()),
                totalWorkOrders,
                totalDowntimes,
                riskScore,
                riskLevel,
                recommendation
        );
    }

    private int calculateRiskScore(Long totalWorkOrders, Long totalDowntimes) {
        int score = 0;

        score += totalWorkOrders * 5;
        score += totalDowntimes * 10;

        return Math.min(score, 100);
    }

    private String getRiskLevel(int score) {
        if (score >= 70) {
            return "HIGH";
        }

        if (score >= 40) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String generateRecommendation(
            Asset asset,
            Long totalWorkOrders,
            Long totalDowntimes,
            int riskScore,
            String riskLevel
    ) {
        String prompt = """
                Bạn là AI hỗ trợ phân tích rủi ro bảo trì trong hệ thống EMMS.

                Dữ liệu thiết bị:
                - Tên thiết bị: %s
                - Trạng thái hiện tại: %s
                - Tổng Work Order: %d
                - Tổng downtime: %d
                - Risk Score: %d/100
                - Risk Level: %s

                Yêu cầu:
                - Viết khuyến nghị bảo trì ngắn gọn bằng tiếng Việt.
                - Không tự tạo thêm số liệu.
                - Nếu rủi ro HIGH: đề xuất ưu tiên kiểm tra/bảo trì.
                - Nếu rủi ro MEDIUM: đề xuất theo dõi và tăng kiểm tra định kỳ.
                - Nếu rủi ro LOW: đề xuất tiếp tục vận hành và theo dõi định kỳ.
                """.formatted(
                asset.getName(),
                asset.getStatus(),
                totalWorkOrders,
                totalDowntimes,
                riskScore,
                riskLevel
        );

        return geminiService.ask(prompt);
    }
}