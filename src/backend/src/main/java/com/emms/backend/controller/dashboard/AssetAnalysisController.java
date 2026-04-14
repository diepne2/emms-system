package com.emms.backend.controller.dashboard;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.dashboard.asset.AssetOverview;
import com.emms.backend.dto.dashboard.asset.AssetStats;
import com.emms.backend.dto.dashboard.asset.DowntimesByAsset;
import com.emms.backend.dto.dashboard.asset.DowntimesByDate;
import com.emms.backend.dto.dashboard.asset.DowntimesMeantimeByDate;
import com.emms.backend.dto.dashboard.asset.Meantimes;
import com.emms.backend.dto.dashboard.asset.MTBFByAsset;
import com.emms.backend.service.dashboard.AssetAnalysisService;
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
@RequestMapping("/api/dashboard/assets")
public class AssetAnalysisController {

    private final AssetAnalysisService assetAnalysisService;

    public AssetAnalysisController(AssetAnalysisService assetAnalysisService) {
        this.assetAnalysisService = assetAnalysisService;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getOverview(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long assetId
    ) {
        AssetOverview data = assetAnalysisService.getAssetOverview(fromDate, toDate, assetId);
        return ResponseEntity.ok(new SuccessResponse(true, "Asset overview fetched successfully", data));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getStats(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long assetId
    ) {
        AssetStats data = assetAnalysisService.getAssetStats(fromDate, toDate, assetId);
        return ResponseEntity.ok(new SuccessResponse(true, "Asset stats fetched successfully", data));
    }

    @GetMapping("/downtimes/by-asset")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getDowntimesByAsset(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<DowntimesByAsset> data = assetAnalysisService.getDowntimesByAsset(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Downtimes by asset fetched successfully", data));
    }

    @GetMapping("/downtimes/by-date")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getDowntimesByDate(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long assetId
    ) {
        List<DowntimesByDate> data = assetAnalysisService.getDowntimesByDate(fromDate, toDate, assetId);
        return ResponseEntity.ok(new SuccessResponse(true, "Downtimes by date fetched successfully", data));
    }

    @GetMapping("/downtimes/meantime-by-date")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getDowntimesMeantimeByDate(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long assetId
    ) {
        List<DowntimesMeantimeByDate> data = assetAnalysisService.getDowntimesMeantimeByDate(fromDate, toDate, assetId);
        return ResponseEntity.ok(new SuccessResponse(true, "Downtime meantime by date fetched successfully", data));
    }

    @GetMapping("/meantimes")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getMeantimes(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long assetId
    ) {
        Meantimes data = assetAnalysisService.getMeantimes(fromDate, toDate, assetId);
        return ResponseEntity.ok(new SuccessResponse(true, "Asset meantimes fetched successfully", data));
    }

    @GetMapping("/mtbf/by-asset")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> getMtbfByAsset(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<MTBFByAsset> data = assetAnalysisService.getMtbfByAsset(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "MTBF by asset fetched successfully", data));
    }
}