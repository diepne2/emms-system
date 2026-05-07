package com.emms.backend.service;

import org.springframework.web.multipart.MultipartFile;

import com.emms.backend.entity.File;

public interface StorageService {

    String upload(MultipartFile file, String folder);

    byte[] download(String filePath);

    byte[] download(File file);

    String generateSignedUrl(String filePath, long expirationMinutes);

    void delete(String filePath);

    default String uploadAndSign(MultipartFile file, String folder) {
        return generateSignedUrl(upload(file, folder), 10);
    }
}