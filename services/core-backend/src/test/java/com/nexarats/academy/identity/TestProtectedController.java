package com.nexarats.academy.identity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestProtectedController {

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student-only")
    public Map<String, String> studentOnly() {
        return Map.of("access", "GRANTED_STUDENT");
    }

    @PreAuthorize("hasRole('ACADEMY_ADMIN')")
    @GetMapping("/admin-only")
    public Map<String, String> adminOnly() {
        return Map.of("access", "GRANTED_ADMIN");
    }

    @PreAuthorize("hasAuthority('finance:write')")
    @GetMapping("/finance-only")
    public Map<String, String> financeOnly() {
        return Map.of("access", "GRANTED_FINANCE");
    }
}
