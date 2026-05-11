package com.emms.backend.dto.ai;

public class AiRiskAssetDTO {

    private Long assetId;
    private String assetName;
    private String status;
    private Long totalWorkOrders;
    private Long totalDowntimes;
    private Integer riskScore;
    private String riskLevel;
    private String recommendation;

    public AiRiskAssetDTO() {
    }

    public AiRiskAssetDTO(
            Long assetId,
            String assetName,
            String status,
            Long totalWorkOrders,
            Long totalDowntimes,
            Integer riskScore,
            String riskLevel,
            String recommendation
    ) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.status = status;
        this.totalWorkOrders = totalWorkOrders;
        this.totalDowntimes = totalDowntimes;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.recommendation = recommendation;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTotalWorkOrders() {
        return totalWorkOrders;
    }

    public void setTotalWorkOrders(Long totalWorkOrders) {
        this.totalWorkOrders = totalWorkOrders;
    }

    public Long getTotalDowntimes() {
        return totalDowntimes;
    }

    public void setTotalDowntimes(Long totalDowntimes) {
        this.totalDowntimes = totalDowntimes;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}