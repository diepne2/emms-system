package com.emms.backend.controller.analystic;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.analystic.user.UserWOStats;
import com.emms.backend.dto.analystic.user.WOStatsByDay;
import com.emms.backend.service.analytics.UserAnalysisService;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard/users")
public class UserAnalysisController {

    private final UserAnalysisService userAnalysisService;

    public UserAnalysisController(UserAnalysisService userAnalysisService) {
        this.userAnalysisService = userAnalysisService;
    }

    @GetMapping("/{userId}/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getUserStats(
            @PathVariable Long userId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        UserWOStats data = userAnalysisService.getUserStats(userId, fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê người dùng đã được truy xuất thành công.", data));
    }

    @GetMapping("/{userId}/stats-by-day")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getStatsByDay(
            @PathVariable Long userId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<WOStatsByDay> data = userAnalysisService.getWOStatsByDay(userId, fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê người dùng theo ngày đã được truy xuất thành công.", data));
    }
}