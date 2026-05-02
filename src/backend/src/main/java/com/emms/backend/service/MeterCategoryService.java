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
            throw new CustomException("Dữ liệu loại công tơ không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (meterCategory.getName() == null || meterCategory.getName().isBlank()) {
            throw new CustomException("Tên loại công tơ không được để trống", HttpStatus.BAD_REQUEST);
        }

        Optional<MeterCategory> categoryWithSameName =
                meterCategoryRepository.findByNameIgnoreCase(meterCategory.getName());

        if (categoryWithSameName.isPresent()) {
            throw new CustomException("Loại công tơ với tên đã tồn tại", HttpStatus.NOT_ACCEPTABLE);
        }

        return meterCategoryRepository.save(meterCategory);
    }

    public MeterCategory update(Long id, CategoryPatchDTO meterCategoryDto) {
        if (id == null) {
            throw new CustomException("ID loại công tơ không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (meterCategoryDto == null) {
            throw new CustomException("Dữ liệu cập nhật loại công tơ không được để trống", HttpStatus.BAD_REQUEST);
        }

        MeterCategory savedMeterCategory = meterCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Loại công tơ không tìm thấy", HttpStatus.NOT_FOUND));

        if (meterCategoryDto.getName() != null && !meterCategoryDto.getName().isBlank()) {
            Optional<MeterCategory> categoryWithSameName =
                    meterCategoryRepository.findByNameIgnoreCase(meterCategoryDto.getName());

            if (categoryWithSameName.isPresent()
                    && !categoryWithSameName.get().getMeterCategoryId().equals(id)) {
                throw new CustomException(
                        "Loại công tơ với tên đã tồn tại",
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
            throw new CustomException("ID loại công tơ không được để trống", HttpStatus.BAD_REQUEST);
        }

        return meterCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Loại công tơ không tìm thấy", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Optional<MeterCategory> findByNameIgnoreCase(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return meterCategoryRepository.findByNameIgnoreCase(name.trim());
    }
}