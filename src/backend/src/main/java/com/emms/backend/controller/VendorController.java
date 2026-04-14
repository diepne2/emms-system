package com.emms.backend.controller;

import com.emms.backend.advancedsearch.SearchCriteria;
import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.vendor.VendorPatchDTO;
import com.emms.backend.entity.Vendor;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    /**
     * Search vendors
     */
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT', 'NHANVIENKYTHUAT', 'NHANVIENVANHANH')")
    public ResponseEntity<Page<Vendor>> search(@RequestBody SearchCriteria searchCriteria) {
        return ResponseEntity.ok(vendorService.findBySearchCriteria(searchCriteria));
    }

    /**
     * Lấy tất cả vendors
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT', 'NHANVIENKYTHUAT', 'NHANVIENVANHANH')")
    public ResponseEntity<Collection<Vendor>> getAll() {
        return ResponseEntity.ok(vendorService.getAll());
    }

    /**
     * Lấy vendor theo id
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT', 'NHANVIENKYTHUAT', 'NHANVIENVANHANH')")
    public ResponseEntity<Vendor> getById(@PathVariable("id") Long id) {
        return vendorService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CustomException("Vendor not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Tạo vendor
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<Vendor> create(@Valid @RequestBody Vendor vendor) {
        Vendor created = vendorService.create(vendor);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật vendor
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<Vendor> patch(
            @PathVariable("id") Long id,
            @Valid @RequestBody VendorPatchDTO dto
    ) {
        return ResponseEntity.ok(vendorService.update(id, dto));
    }

    /**
     * Xóa vendor
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable("id") Long id) {
        vendorService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }

    /**
     * Tìm vendor theo company name
     * Example: GET /api/vendors/by-company-name?companyName=ABC
     */
    @GetMapping("/by-company-name")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT', 'NHANVIENKYTHUAT', 'NHANVIENVANHANH')")
    public ResponseEntity<Vendor> findByCompanyName(@RequestParam("companyName") String companyName) {
        return vendorService.findByCompanyNameIgnoreCase(companyName)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CustomException("Vendor not found", HttpStatus.NOT_FOUND));
    }
}