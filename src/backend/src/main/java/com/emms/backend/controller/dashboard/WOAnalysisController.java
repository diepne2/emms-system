package com.emms.backend.controller.dashboard;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.dashboard.WOCompletedByUser;
import com.emms.backend.dto.dashboard.WOCountByAsset;
import com.emms.backend.dto.dashboard.workorder.IncompleteWOByAsset;
import com.emms.backend.dto.dashboard.workorder.IncompleteWOByUser;
import com.emms.backend.dto.dashboard.workorder.WOCountByUser;
import com.emms.backend.dto.dashboard.workorder.WOCountByWeek;
import com.emms.backend.dto.dashboard.workorder.WOHours;
import com.emms.backend.dto.dashboard.workorder.WOIncompleteStats;
import com.emms.backend.dto.dashboard.workorder.WOStats;
import com.emms.backend.dto.dashboard.workorder.WOStatsByPriority;
import com.emms.backend.dto.dashboard.workorder.WOStatuses;
import com.emms.backend.dto.dashboard.workorder.WOStatusesByDate;
import com.emms.backend.dto.dashboard.workorder.WOTimeByWeek;
import com.emms.backend.service.dashboard.WOAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard/work-orders")
@Tag(name = "WO Analysis", description = "Phân tích và thống kê Work Order")
public class WOAnalysisController {

    private final WOAnalysisService woAnalysisService;

    public WOAnalysisController(WOAnalysisService woAnalysisService) {
        this.woAnalysisService = woAnalysisService;
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thống kê KPI tổng quan work order")
    public ResponseEntity<SuccessResponse> getStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        WOStats data = woAnalysisService.getStats(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Work order stats fetched successfully", data));
    }

    @GetMapping("/statuses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thống kê số lượng work order theo trạng thái")
    public ResponseEntity<SuccessResponse> getStatuses(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        WOStatuses data = woAnalysisService.getStatuses(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Trạng thái lệnh công việc đã được truy xuất thành công.", data));
    }

    @GetMapping("/statuses-by-date")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thống kê số lượng work order theo trạng thái và theo ngày")
    public ResponseEntity<SuccessResponse> getStatusesByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        List<WOStatusesByDate> data = woAnalysisService.getStatusesByDate(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Trạng thái lệnh công việc theo ngày đã được truy xuất thành công.", data));
    }

    @GetMapping("/count-by-week")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thống kê số lượng work order theo tuần")
    public ResponseEntity<SuccessResponse> getCountByWeek(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        List<WOCountByWeek> data = woAnalysisService.getCountByWeek(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Số lượng lệnh công việc theo tuần đã được truy xuất thành công.", data));
    }

    @GetMapping("/time-by-week")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thống kê thời gian work order theo tuần")
    public ResponseEntity<SuccessResponse> getTimeByWeek(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        List<WOTimeByWeek> data = woAnalysisService.getTimeByWeek(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thời gian lệnh công việc theo tuần đã được truy xuất thành công.", data));
    }

    @GetMapping("/count-by-user")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thống kê số lượng work order theo người dùng")
    public ResponseEntity<SuccessResponse> getCountByUser(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        List<WOCountByUser> data = woAnalysisService.getCountByUser(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Số lượng lệnh công việc theo người dùng đã được truy xuất thành công.", data));
    }

    @GetMapping("/hours")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "So sánh thời gian ước tính và thực tế của work order")
    public ResponseEntity<SuccessResponse> getHours(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        WOHours data = woAnalysisService.getHours(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thời gian lệnh công việc đã được truy xuất thành công.", data));
    }

    @GetMapping("/incomplete-stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thống kê work order chưa hoàn thành")
    public ResponseEntity<SuccessResponse> getIncompleteStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        WOIncompleteStats data = woAnalysisService.getIncompleteStats(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê lệnh công việc chưa hoàn thành đã được truy xuất thành công.", data));
    }

    @GetMapping("/incomplete-by-user")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thống kê work order chưa hoàn thành theo người dùng")
    public ResponseEntity<SuccessResponse> getIncompleteByUser(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        List<IncompleteWOByUser> data = woAnalysisService.getIncompleteByUser(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Lệnh công việc chưa hoàn thành theo người dùng đã được truy xuất thành công.", data));
    }

    @GetMapping("/incomplete-by-asset")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thống kê work order chưa hoàn thành theo tài sản")
    public ResponseEntity<SuccessResponse> getIncompleteByAsset(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        List<IncompleteWOByAsset> data = woAnalysisService.getIncompleteByAsset(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Lệnh công việc chưa hoàn thành theo tài sản đã được truy xuất thành công.", data));
    }

    @GetMapping("/priority-stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thống kê work order theo mức độ ưu tiên")
    public ResponseEntity<SuccessResponse> getStatsByPriority(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        WOStatsByPriority data = woAnalysisService.getStatsByPriority(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê lệnh công việc theo mức độ ưu tiên đã được truy xuất thành công.", data));
    }

    @GetMapping("/top-repaired-assets")
    public List<WOCountByAsset> getTop10RepairedAssets(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return woAnalysisService.getTop10RepairedAssets(fromDate, toDate);
    }
    
    
    @GetMapping("/top-completed-users")
    public List<WOCompletedByUser> getTop10CompletedUsers(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return woAnalysisService.getTop10CompletedUsers(fromDate, toDate);
    }
}