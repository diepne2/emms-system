package com.emms.backend.entity.enums;

public enum WFMainCondition {

    // ===== WORK ORDER EVENTS =====
    WORK_ORDER_CREATED,
    WORK_ORDER_UPDATED,
    WORK_ORDER_DELETED,

    // ===== STATUS EVENTS =====
    WORK_ORDER_STATUS_CHANGED,
    WORK_ORDER_COMPLETED,
    WORK_ORDER_APPROVED,
    WORK_ORDER_REJECTED,

    // ===== TIME EVENTS =====
    WORK_ORDER_OVERDUE,
    WORK_ORDER_DUE_SOON,

    // ===== MAINTENANCE PLAN =====
    PM_TRIGGERED,

    // ===== MATERIAL =====
    PART_LOW_STOCK,

    // ===== SYSTEM =====
    MANUAL_TRIGGER
}