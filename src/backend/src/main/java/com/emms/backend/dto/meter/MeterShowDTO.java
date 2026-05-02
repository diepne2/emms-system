package com.emms.backend.dto.meter;

import com.emms.backend.dto.asset.AssetSummaryDTO;
import com.emms.backend.dto.audit.AuditShowDTO;
import com.emms.backend.dto.category.CategorySummaryDTO;
import com.emms.backend.dto.file.FileShowDTO;
import com.emms.backend.dto.location.LocationSummaryDTO;
import com.emms.backend.dto.user.UserSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "DTO hiển thị đầy đủ thông tin đồng hồ (Meter)")
public class MeterShowDTO extends AuditShowDTO {

    @Schema(description = "ID đồng hồ", example = "1")
    private Long id;

    @Schema(description = "Tên đồng hồ", example = "Operating Hours")
    private String name;

    @Schema(description = "Đơn vị đo", example = "hours")
    private String unit;

    @Schema(description = "Tần suất cập nhật", example = "30")
    private Integer updateFrequency;

    @Schema(description = "Danh mục meter")
    private CategorySummaryDTO meterCategory;

    @Schema(description = "Ảnh meter")
    private FileShowDTO image;

    @Schema(description = "Danh sách user được gán")
    private List<UserSummaryDTO> users = new ArrayList<>();

    private LocationSummaryDTO location;

    private AssetSummaryDTO asset;

    @Schema(description = "Là dữ liệu demo hay không")
    private boolean demo;

    @Schema(description = "Thời điểm ghi nhận mới nhất")
    private LocalDateTime lastReading;

    @Schema(description = "Thời điểm cần ghi nhận tiếp theo")
    private LocalDateTime nextReading;

    public MeterShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = trim(name);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = trim(unit);
    }

    public Integer getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(Integer updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public CategorySummaryDTO getMeterCategory() {
        return meterCategory;
    }

    public void setMeterCategory(CategorySummaryDTO meterCategory) {
        this.meterCategory = meterCategory;
    }

    public FileShowDTO getImage() {
        return image;
    }

    public void setImage(FileShowDTO image) {
        this.image = image;
    }

    public List<UserSummaryDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserSummaryDTO> users) {
        this.users = users != null ? users : new ArrayList<>();
    }

    public LocationSummaryDTO getLocation() {
        return location;
    }

    public void setLocation(LocationSummaryDTO location) {
        this.location = location;
    }

    public AssetSummaryDTO getAsset() {
        return asset;
    }

    public void setAsset(AssetSummaryDTO asset) {
        this.asset = asset;
    }

    public boolean isDemo() {
        return demo;
    }

    public void setDemo(boolean demo) {
        this.demo = demo;
    }

    public LocalDateTime getLastReading() {
        return lastReading;
    }

    public void setLastReading(LocalDateTime lastReading) {
        this.lastReading = lastReading;
    }

    public LocalDateTime getNextReading() {
        return nextReading;
    }

    public void setNextReading(LocalDateTime nextReading) {
        this.nextReading = nextReading;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}