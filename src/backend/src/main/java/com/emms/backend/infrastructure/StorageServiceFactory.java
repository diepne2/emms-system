package com.emms.backend.infrastructure;

import org.springframework.context.annotation.Configuration;

import com.emms.backend.entity.enums.StorageType;
import com.emms.backend.service.GCPService;
import com.emms.backend.service.MinioService;
import com.emms.backend.service.StorageService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;


@Configuration
@RequiredArgsConstructor
public class StorageServiceFactory {
    @Value("${storage.type}")
    private StorageType storageType;

    private final GCPService gcpService;
    private final MinioService minioService;

    public StorageService getStorageService() {
        switch (storageType) {
            case GCP:
                return gcpService;
            default:
                return minioService;
        }
    }
}