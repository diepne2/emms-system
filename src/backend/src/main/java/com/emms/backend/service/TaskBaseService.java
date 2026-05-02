package com.emms.backend.service;

import com.emms.backend.dto.task.*;
import com.emms.backend.entity.TaskBase;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.TaskBaseMapper;
import com.emms.backend.repository.TaskBaseRepository;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@Transactional
public class TaskBaseService {

    private final TaskBaseRepository repo;
    private final TaskBaseMapper mapper;
    private final AssetService assetService;
    private final UserService userService;
    private final EntityManager em;

    public TaskBaseService(TaskBaseRepository repo,
                           TaskBaseMapper mapper,
                           AssetService assetService,
                           UserService userService,
                           EntityManager em) {
        this.repo = repo;
        this.mapper = mapper;
        this.assetService = assetService;
        this.userService = userService;
        this.em = em;
    }

    public TaskBase create(TaskBaseDTO dto) {

        if (dto == null) {
            throw new CustomException("DTO bắt buộc không được để trống", HttpStatus.BAD_REQUEST);
        }

        TaskBase entity = mapper.fromDto(dto);

        if (dto.getAssetId() != null) {
            entity.setAsset(assetService.getById(dto.getAssetId()));
        }

        if (dto.getCreatedById() != null) {
            entity.setCreatedBy(userService.findEntityById(dto.getCreatedById()));
        }

        if (dto.getActive() == null) {
            entity.setActive(true);
        }

        TaskBase saved = repo.saveAndFlush(entity);
        em.refresh(saved);

        return saved;
    }

    public TaskBase update(Long id, TaskBasePatchDTO dto) {

        TaskBase existing = repo.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy tác vụ", HttpStatus.NOT_FOUND));

        mapper.update(existing, dto);

        if (dto.getAssetId() != null) {
            existing.setAsset(assetService.getById(dto.getAssetId()));
        }

        TaskBase saved = repo.saveAndFlush(existing);
        em.refresh(saved);

        return saved;
    }

    public TaskBase findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy tác vụ", HttpStatus.NOT_FOUND));
    }

    public Collection<TaskBase> getAll() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.delete(findById(id));
    }
}