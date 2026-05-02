package com.emms.backend.controller;

import com.emms.backend.dto.reading.ReadingDTO;
import com.emms.backend.dto.reading.ReadingShowDTO;
import com.emms.backend.service.ReadingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readings")
public class ReadingController {

    private final ReadingService readingService;

    public ReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN')")
    public ResponseEntity<ReadingShowDTO> create(@Valid @RequestBody ReadingDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(readingService.create(dto));
    }

    @GetMapping("/meter/{meterId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReadingShowDTO>> getByMeter(@PathVariable Long meterId) {
        return ResponseEntity.ok(readingService.getByMeter(meterId));
    }
}