package com.emms.backend.service;

import com.emms.backend.entity.File;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for file storage operations.
 * Supports uploading, downloading, deleting, and generating signed URLs
 * regardless of the underlying storage provider
 * (local disk, S3, MinIO, Firebase, etc.).
 */
public interface StorageService {

    /**
     * Uploads a file to the given folder and returns the stored file path/key.
     *
     * @param file   file to upload
     * @param folder logical folder or prefix
     * @return stored file path/key
     * @throws IllegalArgumentException if file is null or empty
     */
    String upload(MultipartFile file, String folder);

    /**
     * Downloads file content by stored file path/key.
     *
     * @param filePath stored file path/key
     * @return file content as bytes
     * @throws IllegalArgumentException if filePath is blank
     */
    byte[] download(String filePath);

    /**
     * Downloads file content using file metadata entity.
     *
     * @param file file metadata entity
     * @return file content as bytes
     * @throws IllegalArgumentException if file is null
     */
    default byte[] download(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (file.getPath() == null || file.getPath().isBlank()) {
            throw new IllegalArgumentException("File path must not be blank");
        }
        return download(file.getPath());
    }

    /**
     * Generates a signed temporary access URL from file metadata.
     *
     * @param file file metadata entity
     * @param expirationMinutes expiration time in minutes
     * @return signed URL
     * @throws IllegalArgumentException if file is null
     */
    default String generateSignedUrl(File file, long expirationMinutes) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (file.getPath() == null || file.getPath().isBlank()) {
            throw new IllegalArgumentException("File path must not be blank");
        }
        return generateSignedUrl(file.getPath(), expirationMinutes);
    }

    /**
     * Generates a signed temporary access URL from stored file path/key.
     *
     * @param filePath stored file path/key
     * @param expirationMinutes expiration time in minutes
     * @return signed URL
     * @throws IllegalArgumentException if filePath is blank
     * @throws IllegalArgumentException if expirationMinutes <= 0
     */
    String generateSignedUrl(String filePath, long expirationMinutes);

    /**
     * Deletes a file from storage using stored file path/key.
     *
     * @param filePath stored file path/key
     * @throws IllegalArgumentException if filePath is blank
     */
    void delete(String filePath);

    /**
     * Deletes a file from storage using file metadata entity.
     *
     * @param file file metadata entity
     * @throws IllegalArgumentException if file is null
     */
    default void delete(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (file.getPath() == null || file.getPath().isBlank()) {
            throw new IllegalArgumentException("File path must not be blank");
        }
        delete(file.getPath());
    }

    /**
     * Uploads a file and immediately returns a signed URL valid for 10 minutes.
     *
     * @param file   file to upload
     * @param folder logical folder or prefix
     * @return signed URL valid for 10 minutes
     */
    default String uploadAndSign(MultipartFile file, String folder) {
        return generateSignedUrl(upload(file, folder), 10);
    }
}