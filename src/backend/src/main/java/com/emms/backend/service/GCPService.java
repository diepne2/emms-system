package com.emms.backend.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.emms.backend.exception.CustomException;
import com.emms.backend.entity.File;
import com.emms.backend.utils.Helper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class GCPService implements StorageService {

    @Value("${storage.gcp.value:}")
    private String gcpJson;

    @Value("${storage.gcp.json-path:}")
    private String gcpJsonPath;

    @Value("${storage.gcp.project-id:}")
    private String gcpProjectId;

    @Value("${storage.gcp.bucket-name:}")
    private String gcpBucketName;

    private Storage storage;
    private boolean configured = false;

    @PostConstruct
    public void init() {
        if (isBlank(gcpJson) && isBlank(gcpJsonPath)) {
            configured = false;
            return;
        }

        try (InputStream is = buildCredentialStream()) {
            Credentials credentials = GoogleCredentials.fromStream(is);

            this.storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(gcpProjectId)
                    .build()
                    .getService();

            this.configured = true;
        } catch (IOException e) {
            throw new CustomException("Wrong GCP credentials", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String upload(MultipartFile file, String folder) {
        checkIfConfigured();

        if (file == null || file.isEmpty()) {
            throw new CustomException("File không được để trống", HttpStatus.BAD_REQUEST);
        }

        String safeFolder = sanitizeFolder(folder);
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String generatedName = new Helper().generateString() + "_" + originalFilename;
        String filePath = safeFolder + "/" + generatedName;

        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(gcpBucketName, filePath)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(
                    blobInfo,
                    file.getBytes(),
                    Storage.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PRIVATE)
            );

            return filePath;
        } catch (IOException e) {
            throw new CustomException("Không thể đọc file đã tải lên", HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (StorageException e) {
            throw new CustomException("Lỗi khi tải lên file: " + e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public byte[] download(String filePath) {
        checkIfConfigured();

        Blob blob = storage.get(BlobId.of(gcpBucketName, filePath));
        if (blob == null) {
            throw new CustomException("File không tìm thấy", HttpStatus.NOT_FOUND);
        }

        try {
            return blob.getContent();
        } catch (StorageException e) {
            throw new CustomException("Error retrieving file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public byte[] download(File file) {
        checkIfConfigured();

        if (file == null || isBlank(file.getPath())) {
            throw new CustomException("Địa chỉ File không được để trống", HttpStatus.BAD_REQUEST);
        }

        return download(file.getPath());
    }

    public String generateSignedUrl(File file, long expirationMinutes) {
        checkIfConfigured();

        if (file == null || isBlank(file.getPath())) {
            throw new CustomException("Địa chỉ File không được để trống", HttpStatus.BAD_REQUEST);
        }

        return generateSignedUrl(file.getPath(), expirationMinutes);
    }

    public String generateSignedUrl(String filePath, long expirationMinutes) {
        checkIfConfigured();

        Blob blob = getBlob(filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blob.getBlobId())
                .setContentType(blob.getContentType())
                .build();

        return generateSignedUrl(blobInfo, expirationMinutes);
    }

    public String generateSignedUrl(BlobInfo blobInfo, long expirationMinutes) {
        checkIfConfigured();

        if (blobInfo == null) {
            throw new CustomException("BlobInfo là bắt buộc.", HttpStatus.BAD_REQUEST);
        }

        if (expirationMinutes <= 0) {
            throw new CustomException("Thời gian hết hạn phải lớn hơn 0", HttpStatus.BAD_REQUEST);
        }

        try {
            return storage.signUrl(
                    blobInfo,
                    expirationMinutes,
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.withV4Signature()
            ).toString();
        } catch (StorageException e) {
            throw new CustomException(
                    "Error generating signed URL: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private Blob getBlob(String filePath) {
        checkIfConfigured();

        if (isBlank(filePath)) {
            throw new CustomException("Địa chỉ File không được để trống", HttpStatus.BAD_REQUEST);
        }

        Blob blob = storage.get(BlobId.of(gcpBucketName, filePath));
        if (blob == null) {
            throw new CustomException("File không tìm thấy", HttpStatus.NOT_FOUND);
        }

        return blob;
    }

    private InputStream buildCredentialStream() throws IOException {
        if (!isBlank(gcpJson)) {
            return new ByteArrayInputStream(gcpJson.getBytes(StandardCharsets.UTF_8));
        }
        return Files.newInputStream(Paths.get(gcpJsonPath));
    }

    private void checkIfConfigured() {
        if (!configured || storage == null) {
            throw new CustomException(
                    "Google Cloud Storage is not configured. Please define GCP credentials in environment variables",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private String sanitizeFolder(String folder) {
        if (isBlank(folder)) {
            return "uploads";
        }
        return folder.trim().replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String sanitizeFilename(String filename) {
        String safeName = Objects.toString(filename, "file");
        safeName = safeName.replace("\\", "_").replace("/", "_").trim();
        if (safeName.isEmpty()) {
            safeName = "file";
        }
        return safeName;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public void delete(String filePath) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}