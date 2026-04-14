package com.emms.backend.audit;

public final class AuditUserContext {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    private AuditUserContext() {
    }

    public static void setCurrentUsername(String username) {
        CURRENT_USER.set(username);
    }

    public static String getCurrentUsername() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}