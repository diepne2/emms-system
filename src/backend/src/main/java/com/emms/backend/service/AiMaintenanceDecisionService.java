package com.emms.backend.service;

import com.emms.backend.dto.ai.AiDecisionResponse;
import com.emms.backend.entity.Asset;
import com.emms.backend.repository.AssetDowntimeRepository;
import com.emms.backend.repository.AssetRepository;
import com.emms.backend.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AiMaintenanceDecisionService {

    private final AssetRepository assetRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AssetDowntimeRepository assetDowntimeRepository;
    private final GeminiService geminiService;

    public AiMaintenanceDecisionService(
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

    public AiDecisionResponse ask(String question) {
        if (question == null || question.trim().isEmpty()) {
            throw new RuntimeException("Câu hỏi không được để trống");
        }

        String context = buildDecisionContext();

        String prompt = """
                Bạn là AI hỗ trợ ra quyết định bảo trì trong hệ thống EMMS.

                Vai trò:
                - Phân tích dữ liệu vận hành thực tế.
                - Đánh giá rủi ro thiết bị.
                - Đề xuất hướng bảo trì phù hợp.
                - Hỗ trợ quản lý kỹ thuật ra quyết định.

                Quy tắc:
                - Chỉ sử dụng dữ liệu hệ thống được cung cấp.
                - Không tự tạo số liệu.
                - Nếu dữ liệu chưa đủ, hãy nói rõ: "Dữ liệu hiện tại chưa đủ để kết luận".
                - Trả lời bằng tiếng Việt.
                - Trả lời ngắn gọn, rõ ràng.

                DỮ LIỆU HỆ THỐNG:
                %s

                CÂU HỎI NGƯỜI DÙNG:
                %s

                Định dạng trả lời:
                1. Nhận định chính
                2. Cơ sở dữ liệu/số liệu
                3. Khuyến nghị bảo trì
                """.formatted(context, question);

        try {
            String answer = geminiService.ask(prompt);

            if (answer == null || answer.trim().isEmpty()) {
                return new AiDecisionResponse(buildFallbackAnswer(context));
            }

            return new AiDecisionResponse(answer);

        } catch (Exception e) {
            return new AiDecisionResponse(buildFallbackAnswer(context));
        }
    }

    private String buildDecisionContext() {
        List<Asset> assets = assetRepository.findAll();

        StringBuilder sb = new StringBuilder();

        sb.append("TỔNG QUAN HỆ THỐNG\n");
        sb.append("- Tổng số thiết bị: ")
                .append(assetRepository.count())
                .append("\n");

        sb.append("- Tổng số Work Order: ")
                .append(workOrderRepository.count())
                .append("\n\n");

        sb.append("DỮ LIỆU RỦI RO THEO THIẾT BỊ\n");

        assets.stream()
                .sorted(Comparator.comparingInt(this::calculateRiskScore).reversed())
                .forEach(asset -> {
                    Long totalWO = workOrderRepository.countByAssetId(asset.getId());
                    Long totalDowntime = assetDowntimeRepository.countByAssetId(asset.getId());

                    int riskScore = calculateRiskScore(asset);
                    String riskLevel = getRiskLevel(riskScore);

                    sb.append("- Thiết bị: ")
                            .append(asset.getName())
                            .append(" | ID: ")
                            .append(asset.getId())
                            .append(" | Trạng thái: ")
                            .append(asset.getStatus())
                            .append(" | Tổng Work Order: ")
                            .append(totalWO)
                            .append(" | Tổng downtime: ")
                            .append(totalDowntime)
                            .append(" | Risk Score: ")
                            .append(riskScore)
                            .append("/100")
                            .append(" | Risk Level: ")
                            .append(riskLevel)
                            .append("\n");
                });

        return sb.toString();
    }

    private int calculateRiskScore(Asset asset) {
        Long totalWO = workOrderRepository.countByAssetId(asset.getId());
        Long totalDowntime = assetDowntimeRepository.countByAssetId(asset.getId());

        int score = 0;
        score += totalWO * 5;
        score += totalDowntime * 10;

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

    private String buildFallbackAnswer(String context) {
        return """
                1. Nhận định chính
                Không thể gọi Gemini AI tại thời điểm hiện tại. Tuy nhiên, hệ thống vẫn có thể đưa ra nhận định sơ bộ dựa trên dữ liệu Work Order và downtime hiện có.

                2. Cơ sở dữ liệu/số liệu
                Risk Score được tính theo công thức:
                - Mỗi Work Order: +5 điểm
                - Mỗi downtime: +10 điểm
                - Điểm tối đa: 100

                Dữ liệu hệ thống:
                %s

                3. Khuyến nghị bảo trì
                Nên ưu tiên kiểm tra các thiết bị có Risk Score cao, có nhiều Work Order, nhiều downtime hoặc đang ở trạng thái bảo trì/hư hỏng.
                """.formatted(context);
    }
}