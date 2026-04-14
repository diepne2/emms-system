package com.emms.backend.entity.enums;

public enum BasicPermission {

    // ===== USER =====
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,
    USER_VIEW,

    // ===== DEVICE (ASSET) =====
    ASSET_CREATE,
    ASSET_UPDATE,
    ASSET_DELETE,
    ASSET_VIEW,

    // ===== WORK ORDER =====
    WORK_ORDER_CREATE,
    WORK_ORDER_UPDATE,
    WORK_ORDER_DELETE,
    WORK_ORDER_VIEW,
    WORK_ORDER_ASSIGN,
    WORK_ORDER_APPROVE,

    // ===== MAINTENANCE PLAN =====
    PLAN_CREATE,
    PLAN_UPDATE,
    PLAN_DELETE,
    PLAN_VIEW,

    // ===== PART =====
    PART_CREATE,
    PART_UPDATE,
    PART_DELETE,
    PART_VIEW,

    // ===== LOCATION =====
    LOCATION_CREATE,
    LOCATION_UPDATE,
    LOCATION_DELETE,
    LOCATION_VIEW,

    // ===== FILE =====
    FILE_UPLOAD,
    FILE_DELETE,
    FILE_VIEW,

    // ===== DASHBOARD =====
    DASHBOARD_VIEW,

    // ===== SETTINGS =====
    SETTINGS_ACCESS
}