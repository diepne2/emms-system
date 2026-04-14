package com.emms.backend.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.emms.backend.entity.User;

public final class Helper {

    public Helper() {
    }

    // ===== TIME UTILS =====
    public static Date addSeconds(Date date, int seconds) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    // ===== HASH API KEY =====
    public static String hashKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) {
                    hex.append('0');
                }
                hex.append(s);
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash key", e);
        }
    }

    public String generateString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateString'");
    }

    public static Locale getLocale(User actor) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLocale'");
    }

    public static LocalDate dateToLocalDate(Date start) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dateToLocalDate'");
    }

    public static Date localDateToDate(LocalDate monthStart) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'localDateToDate'");
    }

    public static double getDateDiff(Object createdAt, Object createdAt2, TimeUnit hours) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDateDiff'");
    }
}