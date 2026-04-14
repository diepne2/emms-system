package com.emms.backend.controller.dashboard;

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
@RequestMapping("/api/wo-analysis")
@Tag(name = "WO Analysis", description = "Phân tích và thống kê Work Order")
public class WOAnalysisController {

    private final WOAnalysisService woAnalysisService;

    public WOAnalysisController(WOAnalysisService woAnalysisService) {
        this.woAnalysisService = woAnalysisService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    @Operation(summary = "Thống kê KPI tổng quan work order")
    public ResponseEntity<WOStats> getStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(woAnalysisService.getStats(fromDate, toDate));
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    @Operation(summary = "Thống kê số lượng work order theo trạng thái")
    public ResponseEntity<WOStatuses> getStatuses(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(woAnalysisService.getStatuses(fromDate, toDate));
    }

    @GetMapping("/statuses-by-date")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    @Operation(summary = "Thống kê số lượng work order theo trạng thái và theo ngày")
    public ResponseEntity<List<WOStatusesByDate>> getStatusesByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(woAnalysisService.getStatusesByDate(fromDate, toDate));
    }

    @GetMapping("/count-by-week")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    @Operation(summary = "Thống kê số lượng work order theo tuần")
    public ResponseEntity<List<WOCountByWeek>> getCountByWeek(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(woAnalysisService.getCountByWeek(fromDate, toDate));
    }

    @GetMapping("/time-by-week")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    @Operation(summary = "Thống kê thời gian work order theo tuần")
    public ResponseEntity<List<com.emms.backend.dto.dashboard.workorder.WOTimeByWeek>> getTimeByWeek(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(woAnalysisService.getTimeByWeek(fromDate, toDate));
    }

    @GetMapping("/count-by-user")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    @Operation(summary = "Thống kê số lượng work order theo người dùng")
    public ResponseEntity<List<WOCountByUser>> getCountByUser(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(woAnalysisService.getCountByUser(fromDate, toDate));
    }

    @GetMapping("/hours")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    @Operation(summary = "So sánh thời gian ước tính và thực tế của work order")
    public ResponseEntity<WOHours> getHours(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(woAnalysisService.getHours(fromDate, toDate));
    }

    @GetMapping("/incomplete-stats")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    @Operation(summary = "Thống kê work order chưa hoàn thành")
    public ResponseEntity<WOIncompleteStats> getIncompleteStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(woAnalysisService.getIncompleteStats(fromDate, toDate));
    }

    @GetMapping("/incomplete-by-user")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    @Operation(summary = "Thống kê work order chưa hoàn thành theo người dùng")
    public ResponseEntity<List<IncompleteWOByUser>> getIncompleteByUser(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(woAnalysisService.getIncompleteByUser(fromDate, toDate));
    }

    @GetMapping("/incomplete-by-asset")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    @Operation(summary = "Thống kê work order chưa hoàn thành theo tài sản")
    public ResponseEntity<List<IncompleteWOByAsset>> getIncompleteByAsset(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(woAnalysisService.getIncompleteByAsset(fromDate, toDate));
    }

    @GetMapping("/priority-stats")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    @Operation(summary = "Thống kê work order theo mức độ ưu tiên")
    public ResponseEntity<WOStatsByPriority> getStatsByPriority(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return ResponseEntity.ok(woAnalysisService.getStatsByPriority(fromDate, toDate));
    }
}