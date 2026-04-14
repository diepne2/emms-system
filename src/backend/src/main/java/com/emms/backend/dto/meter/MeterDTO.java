package com.emms.backend.dto.meter;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.Collection;

@Schema(description = "DTO dùng để tạo hoặc cập nhật Meter")
public class MeterDTO {

    @Schema(description = "Tên đồng hồ", example = "Operating Hours")
    private String name;

    @Schema(description = "Đơn vị đo", example = "hours")
    private String unit;

    @Schema(description = "Tần suất cập nhật", example = "30")
    private Integer updateFrequency;

    @Schema(description = "ID danh mục meter", example = "1")
    private Long meterCategoryId;

    @Schema(description = "ID file ảnh", example = "10")
    private Long imageId;

    @Schema(description = "ID vị trí", example = "5")
    private Long locationId;

    @Schema(description = "ID tài sản", example = "100")
    private Long assetId;

    @ArraySchema(
            schema = @Schema(description = "User ID"),
            arraySchema = @Schema(description = "Danh sách user được gán")
    )
    private Collection<Long> userIds = new ArrayList<>();

    public MeterDTO() {
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

    public Long getMeterCategoryId() {
        return meterCategoryId;
    }

    public void setMeterCategoryId(Long meterCategoryId) {
        this.meterCategoryId = meterCategoryId;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Collection<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(Collection<Long> userIds) {
        this.userIds = userIds != null ? userIds : new ArrayList<>();
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}