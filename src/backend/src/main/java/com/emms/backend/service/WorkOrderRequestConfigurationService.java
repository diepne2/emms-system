package com.emms.backend.service;

import com.emms.backend.exception.CustomException;
import com.emms.backend.entity.FieldConfiguration;
import com.emms.backend.entity.WorkOrderRequestConfiguration;
import com.emms.backend.repository.WorkOrderRequestConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderRequestConfigurationService {

    private final WorkOrderRequestConfigurationRepository repository;

    // ================= CREATE =================
    public WorkOrderRequestConfiguration create(WorkOrderRequestConfiguration entity) {
        if (entity == null) {
            throw new CustomException("RequestConfiguration must not be null", HttpStatus.BAD_REQUEST);
        }

        // init default nếu chưa có
        if (entity.getFieldConfigurations() == null || entity.getFieldConfigurations().isEmpty()) {
            entity.initDefaultFieldConfigurations();
        }

        // sync parent
        syncFieldConfigurations(entity);

        return repository.save(entity);
    }

    // ================= UPDATE =================
    public WorkOrderRequestConfiguration update(Long id, WorkOrderRequestConfiguration request) {
        WorkOrderRequestConfiguration existing = repository.findById(id)
                .orElseThrow(() -> new CustomException("Configuration not found", HttpStatus.NOT_FOUND));

        if (request.getFieldConfigurations() != null) {
            existing.setFieldConfigurations(request.getFieldConfigurations());
        }

        // sync lại parent
        syncFieldConfigurations(existing);

        return repository.save(existing);
    }

    // ================= GET ALL =================
    @Transactional(readOnly = true)
    public Collection<WorkOrderRequestConfiguration> getAll() {
        return repository.findAll();
    }

    // ================= GET BY ID =================
    @Transactional(readOnly = true)
    public WorkOrderRequestConfiguration findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CustomException("Configuration not found", HttpStatus.NOT_FOUND));
    }

    // ================= DELETE =================
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CustomException("Configuration not found", HttpStatus.NOT_FOUND);
        }
        repository.deleteById(id);
    }

    // ================= HELPER =================
    private void syncFieldConfigurations(WorkOrderRequestConfiguration entity) {
        if (entity.getFieldConfigurations() == null) {
            return;
        }

        for (FieldConfiguration fc : entity.getFieldConfigurations()) {
            fc.setWorkOrderRequestConfiguration(entity);
        }
    }
}