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
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getRequestStats(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        RequestStats data = requestAnalysisService.getRequestStats(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê yêu cầu đã được truy xuất thành công.", data));
    }

    @GetMapping("/by-category")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getCountByCategory(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<CountByCategory> data = requestAnalysisService.getCountByCategory(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê yêu cầu theo danh mục đã được truy xuất thành công.", data));
    }

    @GetMapping("/resolved-by-date")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getRequestsResolvedByDate(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<RequestsResolvedByDate> data = requestAnalysisService.getRequestsResolvedByDate(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê yêu cầu đã được truy xuất thành công.", data));
    }

    @GetMapping("/by-month")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getRequestsByMonth(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<RequestsByMonth> data = requestAnalysisService.getRequestsByMonth(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê yêu cầu theo tháng đã được truy xuất thành công.", data));
    }

    @GetMapping("/by-priority")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getRequestStatsByPriority(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        RequestStatsByPriority data = requestAnalysisService.getRequestStatsByPriority(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê yêu cầu theo mức độ ưu tiên đã được truy xuất thành công.", data));
    }
}