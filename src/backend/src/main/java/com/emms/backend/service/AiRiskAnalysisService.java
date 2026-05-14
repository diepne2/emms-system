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
            result.add(buildRiskDto(asset));
        }

        result.sort(Comparator.comparing(AiRiskAssetDTO::getRiskScore).reversed());

        return result;
    }

    public AiRiskAssetDTO getAssetRiskAnalysisById(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));

        return buildRiskDto(asset);
    }

    private AiRiskAssetDTO buildRiskDto(Asset asset) {

        Long totalWorkOrders = workOrderRepository.countByAssetId(asset.getId());
        Long totalDowntimes = assetDowntimeRepository.countByAssetId(asset.getId());

        int riskScore = calculateRiskScore(
                asset,
                totalWorkOrders,
                totalDowntimes
        );

        String riskLevel = getRiskLevel(riskScore);

        String recommendation = generateRecommendation(
                asset,
                totalWorkOrders,
                totalDowntimes,
                riskScore,
                riskLevel
        );

        boolean earlyWarning = isEarlyWarning(
                asset,
                totalWorkOrders,
                totalDowntimes,
                riskScore
        );

        String warningMessage = buildWarningMessage(
                asset,
                totalWorkOrders,
                totalDowntimes,
                riskScore,
                riskLevel
        );

        return new AiRiskAssetDTO(
                asset.getId(),
                asset.getName(),
                asset.getStatus() == null
                        ? "UNKNOWN"
                        : String.valueOf(asset.getStatus()),
                totalWorkOrders,
                totalDowntimes,
                riskScore,
                riskLevel,
                recommendation,
                earlyWarning,
                warningMessage
        );
    }

    private int calculateRiskScore(
            Asset asset,
            Long totalWorkOrders,
            Long totalDowntimes
    ) {

        long wo = totalWorkOrders == null ? 0 : totalWorkOrders;
        long downtime = totalDowntimes == null ? 0 : totalDowntimes;

        String status = asset.getStatus() == null
                ? ""
                : String.valueOf(asset.getStatus());

        long score = 0;

        // Work Orders
        score += wo * 5;

        // Downtime
        score += downtime * 10;

        // Status Risk
        if ("MAINTENANCE".equalsIgnoreCase(status)) {
            score += 20;
        }

        if ("BREAKDOWN".equalsIgnoreCase(status)) {
            score += 35;
        }

        if ("DOWN".equalsIgnoreCase(status)) {
            score += 40;
        }

        if ("EMERGENCY_SHUTDOWN".equalsIgnoreCase(status)) {
            score += 50;
        }

        if ("DECOMMISSIONED".equalsIgnoreCase(status)) {
            score += 10;
        }

        return (int) Math.min(score, 100);
    }

    private String getRiskLevel(int score) {

        if (score >= 80) {
            return "CRITICAL";
        }

        if (score >= 60) {
            return "HIGH";
        }

        if (score >= 30) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private boolean isEarlyWarning(
            Asset asset,
            Long totalWorkOrders,
            Long totalDowntimes,
            int riskScore
    ) {

        return riskScore >= 60;
    }

    private String buildWarningMessage(
            Asset asset,
            Long totalWorkOrders,
            Long totalDowntimes,
            int riskScore,
            String riskLevel
    ) {

        String status = asset.getStatus() == null
                ? "UNKNOWN"
                : String.valueOf(asset.getStatus());

        if (riskScore >= 80 || "CRITICAL".equals(riskLevel)) {
            return "Nguy cơ hỏng hóc rất cao. Cần kiểm tra và xử lý khẩn cấp.";
        }

        if (riskScore >= 60 || "HIGH".equals(riskLevel)) {
            return "Thiết bị có mức rủi ro cao. Cần ưu tiên kiểm tra và lập kế hoạch bảo trì.";
        }

        if (riskScore >= 30 || "MEDIUM".equals(riskLevel)) {
            return "Thiết bị có dấu hiệu rủi ro tăng. Nên tăng tần suất theo dõi.";
        }

        if ("MAINTENANCE".equalsIgnoreCase(status)) {
            return "Thiết bị đang bảo trì. Theo dõi tiến độ xử lý.";
        }

        return "Thiết bị đang hoạt động ổn định.";
    }

    private String generateRecommendation(
            Asset asset,
            Long totalWorkOrders,
            Long totalDowntimes,
            int riskScore,
            String riskLevel
    ) {

        String fallback = buildFallbackRecommendation(riskLevel);

        try {

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
                    - Nếu rủi ro CRITICAL hoặc HIGH:
                      đề xuất ưu tiên kiểm tra/bảo trì.
                    - Nếu rủi ro MEDIUM:
                      đề xuất tăng kiểm tra định kỳ.
                    - Nếu rủi ro LOW:
                      đề xuất tiếp tục theo dõi định kỳ.
                    """.formatted(
                    asset.getName(),
                    asset.getStatus(),
                    totalWorkOrders == null ? 0 : totalWorkOrders,
                    totalDowntimes == null ? 0 : totalDowntimes,
                    riskScore,
                    riskLevel
            );

            String aiAnswer = geminiService.ask(prompt);

            if (aiAnswer == null || aiAnswer.trim().isEmpty()) {
                return fallback;
            }

            return aiAnswer;

        } catch (Exception e) {
            return fallback;
        }
    }

    private String buildFallbackRecommendation(String riskLevel) {

        if ("CRITICAL".equals(riskLevel)) {
            return "Thiết bị có nguy cơ hỏng hóc rất cao. Cần xử lý và kiểm tra ngay.";
        }

        if ("HIGH".equals(riskLevel)) {
            return "Thiết bị có mức rủi ro cao. Cần ưu tiên bảo trì và theo dõi downtime.";
        }

        if ("MEDIUM".equals(riskLevel)) {
            return "Thiết bị có mức rủi ro trung bình. Nên tăng tần suất kiểm tra định kỳ.";
        }

        return "Thiết bị có mức rủi ro thấp. Có thể tiếp tục vận hành và theo dõi định kỳ.";
    }
}