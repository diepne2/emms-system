package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.category.CategoryPatchDTO;
import com.emms.backend.entity.TimeCategory;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.TimeCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/time-categories")
public class TimeCategoryController {

    private final TimeCategoryService timeCategoryService;

    public TimeCategoryController(TimeCategoryService timeCategoryService) {
        this.timeCategoryService = timeCategoryService;
    }

    /**
     * Lấy tất cả time categories
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT', 'NHANVIENKYTHUAT', 'NHANVIENVANHANH')")
    public ResponseEntity<Collection<TimeCategory>> getAll() {
        return ResponseEntity.ok(timeCategoryService.getAll());
    }

    /**
     * Lấy time category theo id
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT', 'NHANVIENKYTHUAT', 'NHANVIENVANHANH')")
    public ResponseEntity<TimeCategory> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(timeCategoryService.getById(id));
    }

    /**
     * Tạo time category
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<TimeCategory> create(
            @Valid @RequestBody TimeCategory timeCategory
    ) {
        TimeCategory created = timeCategoryService.create(timeCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật time category
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<TimeCategory> patch(
            @PathVariable("id") Long id,
            @Valid @RequestBody CategoryPatchDTO dto
    ) {
        return ResponseEntity.ok(timeCategoryService.update(id, dto));
    }

    /**
     * Xóa time category
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable("id") Long id) {
        timeCategoryService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }
}