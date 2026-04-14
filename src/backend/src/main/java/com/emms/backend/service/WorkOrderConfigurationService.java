package com.emms.backend.service;

import com.emms.backend.exception.CustomException;
import com.emms.backend.entity.WorkOrderConfiguration;
import com.emms.backend.repository.WorkOrderConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderConfigurationService {

    private final WorkOrderConfigurationRepository workOrderConfigurationRepository;

    public WorkOrderConfiguration create(WorkOrderConfiguration config) {
        if (config == null) {
            throw new CustomException("WorkOrderConfiguration is required", HttpStatus.BAD_REQUEST);
        }

        if (config.getConfigCode() == null || config.getConfigCode().trim().isEmpty()) {
            throw new CustomException("configCode is required", HttpStatus.BAD_REQUEST);
        }

        String code = config.getConfigCode().trim();
        config.setConfigCode(code);

   
        Optional<WorkOrderConfiguration> existing =
                workOrderConfigurationRepository.findByConfigCode(code);

        if (existing.isPresent()) {
            throw new CustomException(
                    "WorkOrderConfiguration already exists with this code",
                    HttpStatus.NOT_ACCEPTABLE
            );
        }

        config.initializeDefaultsIfNeeded();

        return workOrderConfigurationRepository.save(config);
    }

    public WorkOrderConfiguration update(Long id, WorkOrderConfiguration payload) {
        if (payload == null) {
            throw new CustomException("WorkOrderConfiguration is required", HttpStatus.BAD_REQUEST);
        }

        WorkOrderConfiguration existing = findEntityById(id);

  
        if (payload.getConfigName() != null) {
            existing.setConfigName(payload.getConfigName().trim());
        }


        if (payload.getActive() != null) {
            existing.setActive(payload.getActive());
        }


        if (payload.getWorkOrderFieldConfigurations() != null
                && !payload.getWorkOrderFieldConfigurations().isEmpty()) {
            existing.setWorkOrderFieldConfigurations(payload.getWorkOrderFieldConfigurations());
        }

        existing.initializeDefaultsIfNeeded();

        return workOrderConfigurationRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Collection<WorkOrderConfiguration> getAll() {
        return workOrderConfigurationRepository.findAll();
    }

    public void delete(Long id) {
        WorkOrderConfiguration existing = findEntityById(id);
        workOrderConfigurationRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public Optional<WorkOrderConfiguration> findById(Long id) {
        return workOrderConfigurationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public WorkOrderConfiguration findEntityById(Long id) {
        return workOrderConfigurationRepository.findById(id)
                .orElseThrow(() -> new CustomException("WorkOrderConfiguration not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Optional<WorkOrderConfiguration> findByConfigCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return workOrderConfigurationRepository.findByConfigCode(code.trim());
    }
}