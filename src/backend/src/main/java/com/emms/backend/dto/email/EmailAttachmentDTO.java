package com.emms.backend.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "DTO for email attachment data")
public class EmailAttachmentDTO {

    @NotBlank
    private String fileName;

    @NotNull
    private byte[] data;

    @NotBlank
    private String contentType;

    private Long size;


    private boolean inline = false;

    private String contentId;

    public EmailAttachmentDTO() {
    }

    public EmailAttachmentDTO(String fileName, byte[] data, String contentType) {
        this.fileName = trim(fileName);
        this.data = data;
        this.contentType = trim(contentType);
        this.size = data != null ? (long) data.length : 0L;
    }

    public EmailAttachmentDTO(String fileName,
                              byte[] data,
                              String contentType,
                              Long size,
                              boolean inline,
                              String contentId) {
        this.fileName = trim(fileName);
        this.data = data;
        this.contentType = trim(contentType);
        this.size = size != null ? size : (data != null ? (long) data.length : 0L);
        this.inline = inline;
        this.contentId = trim(contentId);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = trim(fileName);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        if (this.size == null) {
            this.size = data != null ? (long) data.length : 0L;
        }
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = trim(contentType);
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size != null ? size : 0L;
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = trim(contentId);
    }

    public boolean hasData() {
        return data != null && data.length > 0;
    }

    public boolean isEmpty() {
        return !hasData();
    }

    public long resolvedSize() {
        if (size != null) {
            return size;
        }
        return data != null ? data.length : 0L;
    }

    public String resolvedContentType() {
        return (contentType == null || contentType.isBlank())
                ? "application/octet-stream"
                : contentType;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}