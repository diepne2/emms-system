package com.emms.backend.entity.enums;

public enum Status {

    OPEN,
    IN_PROGRESS,
    ON_HOLD,
    COMPLETED,
    CANCELLED; // nên thêm cho hệ thống thật

    public static Status fromString(String value) {
        if (value == null || value.isBlank()) {
            return OPEN;
        }

        try {
            return Status.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return OPEN;
        }
    }

    public boolean isDone() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean isActive() {
        return this == OPEN || this == IN_PROGRESS || this == ON_HOLD;
    }
}