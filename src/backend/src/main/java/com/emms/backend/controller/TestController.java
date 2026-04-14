package com.emms.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    // ✅ API chỉ cần login (có token)
    @GetMapping("/user")
    public String user() {
        return "Hello User";
    }

    // ✅ API cần permission cụ thể
    @PreAuthorize("hasAuthority('USER:READ')")
    @GetMapping("/user-read")
    public String userRead() {
        return "User Read OK";
    }

    // ✅ API cần admin
    @PreAuthorize("hasAuthority('ADMIN:CREATE')")
    @PostMapping("/admin-create")
    public String adminCreate() {
        return "Admin Create OK";
    }
}