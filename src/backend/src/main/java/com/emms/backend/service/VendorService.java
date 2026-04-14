package com.emms.backend.service;

import com.emms.backend.advancedsearch.SearchCriteria;
import com.emms.backend.advancedsearch.SpecificationBuilder;
import com.emms.backend.dto.vendor.VendorPatchDTO;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.VendorMapper;
import com.emms.backend.entity.Vendor;
import com.emms.backend.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VendorMapper vendorMapper;

    public Vendor create(Vendor vendor) {
        validateVendor(vendor);
        return vendorRepository.save(vendor);
    }

    public Vendor update(Long id, VendorPatchDTO dto) {
        Vendor savedVendor = vendorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Vendor not found", HttpStatus.NOT_FOUND));

        vendorMapper.updateVendor(savedVendor, dto);
        validateVendor(savedVendor);

        return vendorRepository.save(savedVendor);
    }

    @Transactional(readOnly = true)
    public Collection<Vendor> getAll() {
        return vendorRepository.findAll();
    }

    public void delete(Long id) {
        Vendor savedVendor = vendorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Vendor not found", HttpStatus.NOT_FOUND));

        vendorRepository.delete(savedVendor);
    }

    @Transactional(readOnly = true)
    public Optional<Vendor> findById(Long id) {
        return vendorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Vendor findEntityById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Vendor not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Vendor> findBySearchCriteria(SearchCriteria searchCriteria) {
        SpecificationBuilder<Vendor> builder = new SpecificationBuilder<>();
        if (searchCriteria != null && searchCriteria.getFilterFields() != null) {
            searchCriteria.getFilterFields().forEach(builder::with);
        }

        Pageable page = PageRequest.of(
                searchCriteria.getPageNum(),
                searchCriteria.getPageSize(),
                searchCriteria.getDirection(),
                searchCriteria.getSortField()
        );

        return vendorRepository.findAll(builder.build(), page);
    }

    @Transactional(readOnly = true)
    public Optional<Vendor> findByCompanyNameIgnoreCase(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return Optional.empty();
        }
        return vendorRepository.findByCompanyNameIgnoreCase(companyName.trim());
    }

    private void validateVendor(Vendor vendor) {
        if (vendor == null) {
            throw new CustomException("Vendor must not be null", HttpStatus.BAD_REQUEST);
        }

        if (vendor.getCompanyName() == null || vendor.getCompanyName().trim().isEmpty()) {
            throw new CustomException("Company name is required", HttpStatus.BAD_REQUEST);
        }

        if (vendor.getRating() != null && (vendor.getRating() < 0 || vendor.getRating() > 5)) {
            throw new CustomException("Rating must be between 0 and 5", HttpStatus.BAD_REQUEST);
        }

        if (vendor.getEmail() != null && !vendor.getEmail().trim().isEmpty()) {
            vendor.setEmail(vendor.getEmail().trim());
        }

        if (vendor.getPhone() != null && !vendor.getPhone().trim().isEmpty()) {
            vendor.setPhone(vendor.getPhone().trim());
        }

        if (vendor.getVendorCode() != null && !vendor.getVendorCode().trim().isEmpty()) {
            vendor.setVendorCode(vendor.getVendorCode().trim());

            Optional<Vendor> existing = vendorRepository.findByVendorCodeIgnoreCase(vendor.getVendorCode());
            if (existing.isPresent() && !existing.get().getId().equals(vendor.getId())) {
                throw new CustomException("Vendor code already exists", HttpStatus.CONFLICT);
            }
        }

        vendor.setCompanyName(vendor.getCompanyName().trim());

        Optional<Vendor> existingByCompanyName =
                vendorRepository.findByCompanyNameIgnoreCase(vendor.getCompanyName());

        if (existingByCompanyName.isPresent()
                && !existingByCompanyName.get().getId().equals(vendor.getId())) {
            throw new CustomException("Vendor company name already exists", HttpStatus.CONFLICT);
        }
    }
}