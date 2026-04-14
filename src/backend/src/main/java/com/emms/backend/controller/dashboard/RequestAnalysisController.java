package com.emms.backend.controller.dashboard;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.dashboard.request.CountByCategory;
import com.emms.backend.dto.dashboard.request.RequestStats;
import com.emms.backend.dto.dashboard.request.RequestStatsByPriority;
import com.emms.backend.dto.dashboard.request.RequestsByMonth;
import com.emms.backend.dto.dashboard.request.RequestsResolvedByDate;
import com.emms.backend.service.dashboard.RequestAnalysisService;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard/requests")
public class RequestAnalysisController {

    private final RequestAnalysisService requestAnalysisService;

    public RequestAnalysisController(RequestAnalysisService requestAnalysisService) {
        this.requestAnalysisService = requestAnalysisService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getRequestStats(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        RequestStats data = requestAnalysisService.getRequestStats(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Request stats fetched successfully", data));
    }

    @GetMapping("/by-category")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getCountByCategory(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<CountByCategory> data = requestAnalysisService.getCountByCategory(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Request count by category fetched successfully", data));
    }

    @GetMapping("/resolved-by-date")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getRequestsResolvedByDate(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<RequestsResolvedByDate> data = requestAnalysisService.getRequestsResolvedByDate(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Requests resolved by date fetched successfully", data));
    }

    @GetMapping("/by-month")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getRequestsByMonth(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<RequestsByMonth> data = requestAnalysisService.getRequestsByMonth(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Requests by month fetched successfully", data));
    }

    @GetMapping("/by-priority")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getRequestStatsByPriority(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        RequestStatsByPriority data = requestAnalysisService.getRequestStatsByPriority(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Request stats by priority fetched successfully", data));
    }
}