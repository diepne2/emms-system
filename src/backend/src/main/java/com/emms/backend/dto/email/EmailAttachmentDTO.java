package com.emms.backend.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "DTO for email attachment data")
public class EmailAttachmentDTO {

    @NotBlank(message = "File name must not be blank")
    @Schema(description = "Original attachment file name", example = "maintenance-report.pdf")
    private String fileName;

    @NotNull(message = "Attachment data must not be null")
    @Schema(description = "Attachment binary content")
    private byte[] data;

    @NotBlank(message = "Content type must not be blank")
    @Schema(description = "Attachment MIME type", example = "application/pdf")
    private String contentType;

    @PositiveOrZero(message = "File size must be greater than or equal to 0")
    @Schema(description = "Attachment size in bytes", example = "245760")
    private Long size;

    @Schema(description = "Whether attachment is inline in email body", example = "false")
    private boolean inline = false;

    @Schema(description = "Content ID used for inline attachments", example = "logo_cid")
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