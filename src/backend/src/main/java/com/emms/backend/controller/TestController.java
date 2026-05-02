package com.emms.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

   
    @GetMapping("/user")
    public String user() {
        return "Hello User";
    }


    @PreAuthorize("hasAuthority('USER:READ')")
    @GetMapping("/user-read")
    public String userRead() {
        return "User Read OK";
    }


    @PreAuthorize("hasAuthority('ADMIN:CREATE')")
    @PostMapping("/admin-create")
    public String adminCreate() {
        return "Admin Create OK";
    }
}