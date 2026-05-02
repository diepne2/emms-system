package com.emms.backend.service;

import com.emms.backend.dto.file.FileShowDTO;
import com.emms.backend.dto.file.FileSummaryDTO;
import com.emms.backend.entity.File;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.FileMapper;
import com.emms.backend.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FileService {

    private final FileRepository fileRepository;
    private final FileMapper fileMapper;

    public File create(File file) {
        if (file == null) {
            throw new CustomException("File không được để trống", HttpStatus.BAD_REQUEST);
        }
        return fileRepository.save(file);
    }

    public File update(File file) {
        if (file == null) {
            throw new CustomException("File không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (file.getId() == null) {
            throw new CustomException("ID file không được để trống", HttpStatus.BAD_REQUEST);
        }

        File existing = fileRepository.findById(file.getId())
                .orElseThrow(() -> new CustomException("File không tìm thấy", HttpStatus.NOT_FOUND));

        existing.setName(file.getName());
        existing.setStoredFileName(file.getStoredFileName());
        existing.setPath(file.getPath());
        existing.setFileMimeType(file.getFileMimeType());
        existing.setType(file.getType());
        existing.setHidden(file.isHidden());
        existing.setFileSize(file.getFileSize());
        existing.setTask(file.getTask());
        existing.setUploadedBy(file.getUploadedBy());
        existing.setComment(file.getComment());

        return fileRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public List<File> getAllEntities() {
        return fileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FileShowDTO> getAll() {
        return fileRepository.findAll()
                .stream()
                .map(fileMapper::toShowDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FileSummaryDTO> getAllSummary() {
        return fileRepository.findAll()
                .stream()
                .map(fileMapper::toSummaryDto)
                .toList();
    }

    public void delete(Long id) {
        if (id == null) {
            throw new CustomException("ID file không được để trống", HttpStatus.BAD_REQUEST);
        }

        File existing = fileRepository.findById(id)
                .orElseThrow(() -> new CustomException("File không tìm thấy", HttpStatus.NOT_FOUND));

        fileRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public File findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("ID file không được để trống", HttpStatus.BAD_REQUEST);
        }

        return fileRepository.findById(id)
                .orElseThrow(() -> new CustomException("File không tìm thấy", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public FileShowDTO getById(Long id) {
        return fileMapper.toShowDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public FileSummaryDTO getSummaryById(Long id) {
        return fileMapper.toSummaryDto(findEntityById(id));
    }
}