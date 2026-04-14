package com.emms.backend.entity.enums;

public enum AssetStatus {

    OPERATIONAL(RealStatus.UP),
    DOWN(RealStatus.DOWN),
    MAINTENANCE(RealStatus.DOWN),
    STANDBY(RealStatus.UP),
    INSPECTION(RealStatus.UP),
    COMMISSIONING(RealStatus.UP),
    EMERGENCY_SHUTDOWN(RealStatus.DOWN);

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

    // parse từ string (đơn giản hơn bản gốc)
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