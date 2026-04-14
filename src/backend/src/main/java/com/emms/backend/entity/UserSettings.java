package com.emms.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_settings_id")
    private Long userSettingsId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ===== Notification Settings =====

    @Column(name = "email_notified", nullable = false)
    private boolean emailNotified = true;

    @Column(name = "email_updates_work_orders", nullable = false)
    private boolean emailUpdatesForWorkOrders = true;

    @Column(name = "email_updates_requests", nullable = false)
    private boolean emailUpdatesForRequests = true;

    @Column(name = "email_updates_purchase_orders", nullable = false)
    private boolean emailUpdatesForPurchaseOrders = true;

    // ===== UI Settings =====

    @Column(name = "stats_assigned_work_orders", nullable = false)
    private boolean statsForAssignedWorkOrders = true;

    // ===== Constructor =====

    public UserSettings() {
    }

    public UserSettings(User user) {
        setUser(user);
    }

    // ===== Getter Setter =====

    public Long getUserSettingsId() {
        return userSettingsId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User không được null");
        }
        this.user = user;
    }

    public boolean isEmailNotified() {
        return emailNotified;
    }

    public void setEmailNotified(boolean emailNotified) {
        this.emailNotified = emailNotified;
    }

    public boolean isEmailUpdatesForWorkOrders() {
        return emailUpdatesForWorkOrders;
    }

    public void setEmailUpdatesForWorkOrders(boolean emailUpdatesForWorkOrders) {
        this.emailUpdatesForWorkOrders = emailUpdatesForWorkOrders;
    }

    public boolean isEmailUpdatesForRequests() {
        return emailUpdatesForRequests;
    }

    public void setEmailUpdatesForRequests(boolean emailUpdatesForRequests) {
        this.emailUpdatesForRequests = emailUpdatesForRequests;
    }

    public boolean isEmailUpdatesForPurchaseOrders() {
        return emailUpdatesForPurchaseOrders;
    }

    public void setEmailUpdatesForPurchaseOrders(boolean emailUpdatesForPurchaseOrders) {
        this.emailUpdatesForPurchaseOrders = emailUpdatesForPurchaseOrders;
    }

    public boolean isStatsForAssignedWorkOrders() {
        return statsForAssignedWorkOrders;
    }

    public void setStatsForAssignedWorkOrders(boolean statsForAssignedWorkOrders) {
        this.statsForAssignedWorkOrders = statsForAssignedWorkOrders;
    }

    // ===== Business Logic (IMPORTANT) =====

    public boolean shouldEmailUpdatesForWorkOrders() {
        return emailNotified && emailUpdatesForWorkOrders;
    }

    public boolean shouldEmailUpdatesForRequests() {
        return emailNotified && emailUpdatesForRequests;
    }

    public boolean shouldEmailUpdatesForPurchaseOrders() {
        return emailNotified && emailUpdatesForPurchaseOrders;
    }

    public boolean shouldShowStatsForAssignedWorkOrders() {
        return statsForAssignedWorkOrders; // ❗ FIX: không phụ thuộc email
    }
}