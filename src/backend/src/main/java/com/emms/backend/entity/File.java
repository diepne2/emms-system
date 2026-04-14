package com.emms.backend.entity;

import com.emms.backend.entity.enums.FileType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "files", indexes = {
        @Index(name = "idx_file_task", columnList = "task_id"),
        @Index(name = "idx_file_type", columnList = "type"),
        @Index(name = "idx_file_uploaded_at", columnList = "uploaded_at")
})
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "stored_file_name", nullable = false, unique = true, length = 255)
    private String storedFileName;

    @Column(name = "path", nullable = false, length = 500)
    private String path;

    @Column(name = "file_type", length = 100)
    private String fileMimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private FileType type = FileType.OTHER;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    public File() {
    }

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
        normalize();
    }

    @PreUpdate
    public void preUpdate() {
        normalize();
    }

    private void normalize() {
        name = trim(name);
        storedFileName = trim(storedFileName);
        path = trim(path);
        fileMimeType = trim(fileMimeType);
        uploadedBy = trim(uploadedBy);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name không được để trống");
        }
        if (storedFileName == null || storedFileName.isBlank()) {
            throw new IllegalArgumentException("storedFileName không được để trống");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path không được để trống");
        }
        if (type == null) {
            type = FileType.OTHER;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
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

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = trim(storedFileName);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = trim(path);
    }

    public String getFileMimeType() {
        return fileMimeType;
    }

    public void setFileMimeType(String fileMimeType) {
        this.fileMimeType = trim(fileMimeType);
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = trim(uploadedBy);
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}