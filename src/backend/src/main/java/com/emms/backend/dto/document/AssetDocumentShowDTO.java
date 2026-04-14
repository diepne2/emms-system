package com.emms.backend.dto.document;

import com.emms.backend.dto.file.FileShowDTO;

public class AssetDocumentShowDTO extends FileShowDTO {

    private Long assetId;

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }
}