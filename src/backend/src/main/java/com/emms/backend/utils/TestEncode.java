package com.emms.backend.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestEncode {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashed = encoder.encode("Admin@123");
        System.out.println("Hash BCrypt: " + hashed);
    }
}