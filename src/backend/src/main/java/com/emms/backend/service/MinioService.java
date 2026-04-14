package com.emms.backend.service;

import com.emms.backend.entity.File;
import com.emms.backend.exception.CustomException;
import com.emms.backend.utils.Helper;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService implements StorageService {

    @Value("${storage.minio.endpoint:}")
    private String minioEndpoint;

    @Value("${storage.minio.bucket:}")
    private String minioBucket;

    @Value("${storage.minio.access-key:}")
    private String minioAccessKey;

    @Value("${storage.minio.secret-key:}")
    private String minioSecretKey;

    @Value("${storage.minio.public-endpoint:}")
    private String minioPublicEndpoint;

    private MinioClient minioClient;
    private boolean configured = false;

    @PostConstruct
    public void init() {
        if (isBlank(minioEndpoint)
                || isBlank(minioBucket)
                || isBlank(minioAccessKey)
                || isBlank(minioSecretKey)
                || isBlank(minioPublicEndpoint)) {
            configured = false;
            return;
        }

        try {
            URI minioEndpointUri = new URI(minioEndpoint);

            MinioClient.Builder builder = MinioClient.builder()
                    .endpoint(minioPublicEndpoint)
                    .credentials(minioAccessKey, minioSecretKey);

            if (isLocalhost(minioPublicEndpoint)) {
                builder.httpClient(
                        new OkHttpClient.Builder()
                                .proxy(new Proxy(
                                        Proxy.Type.HTTP,
                                        new InetSocketAddress(
                                                minioEndpointUri.getHost(),
                                                minioEndpointUri.getPort()
                                        )
                                ))
                                .build()
                );
            }

            this.minioClient = builder.build();

            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioBucket).build()
            );

            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioBucket).build()
                );
            }

            this.configured = true;
        } catch (URISyntaxException e) {
            throw new CustomException("Invalid MinIO endpoint", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CustomException("Error configuring MinIO: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String upload(MultipartFile file, String folder) {
        checkIfConfigured();

        if (file == null || file.isEmpty()) {
            throw new CustomException("File must not be empty", HttpStatus.BAD_REQUEST);
        }

        String safeFolder = sanitizeFolder(folder);
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String filePath = safeFolder + "/" + new Helper().generateString() + "_" + originalFilename;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioBucket)
                            .object(filePath)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return filePath;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CustomException("Error uploading file: " + e.getMessage(),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public String generateSignedUrl(File file, long expirationMinutes) {
        checkIfConfigured();

        if (file == null || isBlank(file.getPath())) {
            throw new CustomException("File path is required", HttpStatus.BAD_REQUEST);
        }

        return generateSignedUrl(file.getPath(), expirationMinutes);
    }

    public String generateSignedUrl(String filePath, long expirationMinutes) {
        checkIfConfigured();

        if (isBlank(filePath)) {
            throw new CustomException("File path is required", HttpStatus.BAD_REQUEST);
        }

        if (expirationMinutes <= 0) {
            throw new CustomException("Expiration minutes must be greater than 0", HttpStatus.BAD_REQUEST);
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioBucket)
                            .object(filePath)
                            .expiry(Math.toIntExact(expirationMinutes), TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new CustomException("Error generating signed URL: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public byte[] download(String filePath) {
        checkIfConfigured();

        if (isBlank(filePath)) {
            throw new CustomException("File path is required", HttpStatus.BAD_REQUEST);
        }

        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioBucket)
                        .object(filePath)
                        .build());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CustomException("Error retrieving file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public byte[] download(File file) {
        checkIfConfigured();

        if (file == null || isBlank(file.getPath())) {
            throw new CustomException("File path is required", HttpStatus.BAD_REQUEST);
        }

        return download(file.getPath());
    }

    private void checkIfConfigured() {
        if (!configured || minioClient == null) {
            throw new CustomException(
                    "MinIO is not configured. Please define MinIO credentials in environment variables",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private String sanitizeFolder(String folder) {
        if (isBlank(folder)) {
            return "uploads";
        }
        return folder.trim()
                .replace("\\", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");
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

    private boolean isLocalhost(String endpoint) {
        if (isBlank(endpoint)) {
            return false;
        }
        String value = endpoint.toLowerCase();
        return value.contains("localhost") || value.contains("127.0.0.1");
    }

    @Override
    public void delete(String filePath) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}