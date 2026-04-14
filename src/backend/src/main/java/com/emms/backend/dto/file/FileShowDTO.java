package com.emms.backend.dto.file;

import com.emms.backend.dto.audit.AuditShowDTO;
import com.emms.backend.entity.enums.FileType;

public class FileShowDTO extends AuditShowDTO {

    private Long id;
    private String name;
    private String url;
    private String fileMimeType;
    private Long fileSize;
    private FileType type = FileType.OTHER;
    private boolean hidden = false;
    private String uploadedBy;

    public FileShowDTO() {
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = trim(url);
    }

    public String getFileMimeType() {
        return fileMimeType;
    }

    public void setFileMimeType(String fileMimeType) {
        this.fileMimeType = trim(fileMimeType);
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type == null ? FileType.OTHER : type;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = trim(uploadedBy);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}