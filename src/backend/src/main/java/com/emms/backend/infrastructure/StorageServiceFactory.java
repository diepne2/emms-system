package com.emms.backend.infrastructure;

import com.emms.backend.entity.enums.StorageType;
import com.emms.backend.service.GCPService;
import com.emms.backend.service.MinioService;
import com.emms.backend.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StorageServiceFactory {

    @Value("${storage.type:MINIO}")
    private StorageType storageType;

    private final GCPService gcpService;
    private final MinioService minioService;

    public StorageService getStorageService() {
        return switch (storageType) {
            case GCP -> gcpService;
            case MINIO -> minioService;
        };
    }
}