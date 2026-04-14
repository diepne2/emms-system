package com.emms.backend.service;

import com.emms.backend.dto.category.CategoryPatchDTO;
import com.emms.backend.entity.MeterCategory;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.MeterCategoryMapper;
import com.emms.backend.repository.MeterCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MeterCategoryService {

    private final MeterCategoryRepository meterCategoryRepository;
    private final MeterCategoryMapper meterCategoryMapper;

    public MeterCategory create(MeterCategory meterCategory) {
        if (meterCategory == null) {
            throw new CustomException("Meter category must not be null", HttpStatus.BAD_REQUEST);
        }

        if (meterCategory.getName() == null || meterCategory.getName().isBlank()) {
            throw new CustomException("Meter category name must not be blank", HttpStatus.BAD_REQUEST);
        }

        Optional<MeterCategory> categoryWithSameName =
                meterCategoryRepository.findByNameIgnoreCase(meterCategory.getName());

        if (categoryWithSameName.isPresent()) {
            throw new CustomException("Meter category with same name already exists", HttpStatus.NOT_ACCEPTABLE);
        }

        return meterCategoryRepository.save(meterCategory);
    }

    public MeterCategory update(Long id, CategoryPatchDTO meterCategoryDto) {
        if (id == null) {
            throw new CustomException("Meter category id must not be null", HttpStatus.BAD_REQUEST);
        }

        if (meterCategoryDto == null) {
            throw new CustomException("Meter category patch data must not be null", HttpStatus.BAD_REQUEST);
        }

        MeterCategory savedMeterCategory = meterCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Meter category not found", HttpStatus.NOT_FOUND));

        if (meterCategoryDto.getName() != null && !meterCategoryDto.getName().isBlank()) {
            Optional<MeterCategory> categoryWithSameName =
                    meterCategoryRepository.findByNameIgnoreCase(meterCategoryDto.getName());

            if (categoryWithSameName.isPresent()
                    && !categoryWithSameName.get().getMeterCategoryId().equals(id)) {
                throw new CustomException(
                        "Meter category with same name already exists",
                        HttpStatus.NOT_ACCEPTABLE
                );
            }
        }

        MeterCategory updated = meterCategoryMapper.updateMeterCategory(savedMeterCategory, meterCategoryDto);
        return meterCategoryRepository.save(updated);
    }

    @Transactional(readOnly = true)
    public Collection<MeterCategory> getAll() {
        return meterCategoryRepository.findAll();
    }

    public void delete(Long id) {
        MeterCategory existing = findEntityById(id);
        meterCategoryRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public Optional<MeterCategory> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return meterCategoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public MeterCategory findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("Meter category id must not be null", HttpStatus.BAD_REQUEST);
        }

        return meterCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Meter category not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Optional<MeterCategory> findByNameIgnoreCase(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return meterCategoryRepository.findByNameIgnoreCase(name.trim());
    }
}