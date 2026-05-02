package com.emms.backend.controller.dashboard;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.dashboard.asset.*;
import com.emms.backend.service.dashboard.AssetAnalysisService;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getOverview(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String assetName
    ) {
        AssetOverview data = assetAnalysisService.getAssetOverview(fromDate, toDate, assetName);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê thiết bị đã được truy xuất thành công.", data));
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getStats(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String assetName
    ) {
        AssetStats data = assetAnalysisService.getAssetStats(fromDate, toDate, assetName);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê thiết bị đã được truy xuất thành công.", data));
    }

    @GetMapping("/downtimes/by-asset")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getDowntimesByAsset(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<DowntimesByAsset> data = assetAnalysisService.getDowntimesByAsset(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê thời gian ngừng hoạt động theo thiết bị đã được truy xuất thành công.", data));
    }

    @GetMapping("/downtimes/by-date")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getDowntimesByDate(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String assetName
    ) {
        List<DowntimesByDate> data = assetAnalysisService.getDowntimesByDate(fromDate, toDate, assetName);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê thời gian ngừng hoạt động theo ngày đã được truy xuất thành công.", data));
    }

    @GetMapping("/downtimes/meantime-by-date")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getDowntimesMeantimeByDate(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String assetName
    ) {
        List<DowntimesMeantimeByDate> data =
                assetAnalysisService.getDowntimesMeantimeByDate(fromDate, toDate, assetName);

        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê thời gian ngừng hoạt động trung bình theo ngày đã được truy xuất thành công.", data));
    }

    @GetMapping("/meantimes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getMeantimes(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String assetName
    ) {
        Meantimes data = assetAnalysisService.getMeantimes(fromDate, toDate, assetName);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê thời gian hoạt động trung bình của thiết bị đã được truy xuất thành công.", data));
    }

    @GetMapping("/mtbf/by-asset")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getMtbfByAsset(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<MTBFByAsset> data = assetAnalysisService.getMtbfByAsset(fromDate, toDate);
        return ResponseEntity.ok(new SuccessResponse(true, "Thống kê MTBF theo thiết bị đã được truy xuất thành công.", data));
    }



    @GetMapping("/options")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> getAssetOptions() {
        return ResponseEntity.ok(
            new SuccessResponse(
            true,
            "Asset options fetched successfully",
            assetAnalysisService.getAssetOptions()
        )
    );}
}
