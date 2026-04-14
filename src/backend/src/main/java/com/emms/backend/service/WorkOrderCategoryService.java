package com.emms.backend.service;

import com.emms.backend.dto.category.CategoryPatchDTO;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.WorkOrderCategoryMapper;
import com.emms.backend.entity.WorkOrderCategory;
import com.emms.backend.repository.WorkOrderCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderCategoryService {

    private final WorkOrderCategoryRepository workOrderCategoryRepository;
    private final WorkOrderCategoryMapper workOrderCategoryMapper;

    public WorkOrderCategory create(WorkOrderCategory workOrderCategory) {
        if (workOrderCategory == null) {
            throw new CustomException("Work order category is required", HttpStatus.BAD_REQUEST);
        }

        if (workOrderCategory.getName() == null || workOrderCategory.getName().trim().isEmpty()) {
            throw new CustomException("Work order category name is required", HttpStatus.BAD_REQUEST);
        }

        String normalizedName = workOrderCategory.getName().trim();
        workOrderCategory.setName(normalizedName);

        Optional<WorkOrderCategory> categoryWithSameName =
                workOrderCategoryRepository.findByNameIgnoreCase(normalizedName);

        if (categoryWithSameName.isPresent()) {
            throw new CustomException(
                    "WorkOrderCategory with same name already exists",
                    HttpStatus.NOT_ACCEPTABLE
            );
        }

        return workOrderCategoryRepository.save(workOrderCategory);
    }

    public WorkOrderCategory update(Long id, CategoryPatchDTO dto) {
        WorkOrderCategory existing = findEntityById(id);

        if (dto == null) {
            return existing;
        }

        String oldName = existing.getName();

        if (dto.getName() != null) {
            String newName = dto.getName().trim();

            if (newName.isEmpty()) {
                throw new CustomException(
                        "Work order category name must not be blank",
                        HttpStatus.BAD_REQUEST
                );
            }

            boolean changedName = oldName == null || !oldName.equalsIgnoreCase(newName);

            if (changedName) {
                Optional<WorkOrderCategory> duplicated =
                        workOrderCategoryRepository.findByNameIgnoreCase(newName);

                if (duplicated.isPresent() && !duplicated.get().getId().equals(id)) {
                    throw new CustomException(
                            "WorkOrderCategory with same name already exists",
                            HttpStatus.NOT_ACCEPTABLE
                    );
                }
            }

            dto.setName(newName);
        }

        workOrderCategoryMapper.updateWorkOrderCategory(existing, dto);
        return workOrderCategoryRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Collection<WorkOrderCategory> getAll() {
        return workOrderCategoryRepository.findAll();
    }

    public void delete(Long id) {
        WorkOrderCategory existing = findEntityById(id);
        workOrderCategoryRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public Optional<WorkOrderCategory> findById(Long id) {
        return workOrderCategoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public WorkOrderCategory findEntityById(Long id) {
        return workOrderCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("WorkOrderCategory not found", HttpStatus.NOT_FOUND));
    }

    public Collection<WorkOrderCategory> findAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }
}