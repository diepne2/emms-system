package com.emms.backend.entity.enums;

public enum AssetStatus {

    OPERATIONAL(RealStatus.UP),
    STANDBY(RealStatus.UP),
    INSPECTION(RealStatus.UP),
    COMMISSIONING(RealStatus.UP),

    BREAKDOWN(RealStatus.DOWN),          // Hư hỏng -> tạo downtime
    DOWN(RealStatus.DOWN),
    MAINTENANCE(RealStatus.DOWN),        // Bảo trì kế hoạch
    EMERGENCY_SHUTDOWN(RealStatus.DOWN), // Dừng khẩn cấp -> tạo downtime

    DECOMMISSIONED(RealStatus.DOWN);

    private final RealStatus realStatus;

    AssetStatus(RealStatus realStatus) {
        this.realStatus = realStatus;
    }

    private enum RealStatus {
        UP,
        DOWN
    }

    public boolean isDown() {
        return this.realStatus == RealStatus.DOWN;
    }

    public boolean isUp() {
        return this.realStatus == RealStatus.UP;
    }

    public boolean isDecommissioned() {
        return this == DECOMMISSIONED;
    }

    public boolean isActive() {
        return this != DECOMMISSIONED;
    }

    public boolean shouldCreateDowntime() {
        return this == BREAKDOWN || this == EMERGENCY_SHUTDOWN;
    }

    public static AssetStatus fromString(String value) {
        if (value == null) {
            return OPERATIONAL;
        }

        try {
            return AssetStatus.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return OPERATIONAL;
        }
    }
}