package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.file.FileShowDTO;
import com.emms.backend.dto.file.FileSummaryDTO;
import com.emms.backend.entity.File;
import com.emms.backend.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity<FileShowDTO> create(@Valid @RequestBody File request) {
        File created = fileService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.getById(created.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FileShowDTO> update(@PathVariable Long id,
                                              @Valid @RequestBody File request) {
        request.setId(id);
        File updated = fileService.update(request);
        return ResponseEntity.ok(fileService.getById(updated.getId()));
    }

    @GetMapping
    public ResponseEntity<List<FileShowDTO>> getAll() {
        return ResponseEntity.ok(fileService.getAll());
    }

    @GetMapping("/summary")
    public ResponseEntity<List<FileSummaryDTO>> getAllSummary() {
        return ResponseEntity.ok(fileService.getAllSummary());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileShowDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getById(id));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<FileSummaryDTO> getSummaryById(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getSummaryById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        fileService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Xóa file thành công"));
    }
}