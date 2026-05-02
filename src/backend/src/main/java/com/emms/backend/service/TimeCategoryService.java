package com.emms.backend.service;

import com.emms.backend.dto.category.CategoryPatchDTO;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.TimeCategoryMapper;
import com.emms.backend.entity.TimeCategory;
import com.emms.backend.repository.TimeCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@Transactional
public class TimeCategoryService {

    private final TimeCategoryRepository timeCategoryRepository;
    private final TimeCategoryMapper timeCategoryMapper;

    public TimeCategoryService(TimeCategoryRepository timeCategoryRepository,
                               TimeCategoryMapper timeCategoryMapper) {
        this.timeCategoryRepository = timeCategoryRepository;
        this.timeCategoryMapper = timeCategoryMapper;
    }

    public TimeCategory create(TimeCategory timeCategory) {
        if (timeCategory == null) {
            throw new CustomException("Dữ liệu thời gian danh mục không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (timeCategory.getName() == null || timeCategory.getName().isBlank()) {
            throw new CustomException("Tên thời gian danh mục không được để trống", HttpStatus.BAD_REQUEST);
        }

        Optional<TimeCategory> categoryWithSameName =
                timeCategoryRepository.findByNameIgnoreCase(timeCategory.getName());

        if (categoryWithSameName.isPresent()) {
            throw new CustomException("Thời gian danh mục với tên tương tự đã tồn tại", HttpStatus.NOT_ACCEPTABLE);
        }

        return timeCategoryRepository.save(timeCategory);
    }

    public TimeCategory update(Long id, CategoryPatchDTO dto) {
        TimeCategory savedTimeCategory = timeCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy danh mục thời gian", HttpStatus.NOT_FOUND));

        if (dto != null && dto.getName() != null) {
            Optional<TimeCategory> categoryWithSameName =
                    timeCategoryRepository.findByNameIgnoreCase(dto.getName());

            if (categoryWithSameName.isPresent()
                    && !categoryWithSameName.get().getTimeCategoryId().equals(id)) {
                throw new CustomException("Thời gian danh mục với tên tương tự đã tồn tại", HttpStatus.NOT_ACCEPTABLE);
            }
        }

        timeCategoryMapper.updateTimeCategory(savedTimeCategory, dto);
        return timeCategoryRepository.save(savedTimeCategory);
    }

    @Transactional(readOnly = true)
    public Collection<TimeCategory> getAll() {
        return timeCategoryRepository.findAll();
    }

    public void delete(Long id) {
        if (!timeCategoryRepository.existsById(id)) {
            throw new CustomException("Không tìm thấy danh mục thời gian", HttpStatus.NOT_FOUND);
        }
        timeCategoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<TimeCategory> findById(Long id) {
        return timeCategoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public TimeCategory getById(Long id) {
        return timeCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy danh mục thời gian", HttpStatus.NOT_FOUND));
    }

    public TimeCategory findEntityById(Long id) {
    if (id == null) {
        throw new CustomException("ID danh mục thời gian không được để trống", HttpStatus.BAD_REQUEST);
    }

    return timeCategoryRepository.findById(id)
            .orElseThrow(() -> new CustomException("Không tìm thấy danh mục thời gian", HttpStatus.NOT_FOUND));
}
}