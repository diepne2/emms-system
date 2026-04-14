package com.emms.backend.service;

import com.emms.backend.dto.fieldConfiguration.FieldConfigurationPatchDTO;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.FieldConfigurationMapper;
import com.emms.backend.entity.FieldConfiguration;
import com.emms.backend.repository.FieldConfigurationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@Transactional
public class FieldConfigurationService {

    private final FieldConfigurationRepository fieldConfigurationRepository;
    private final FieldConfigurationMapper fieldConfigurationMapper;

    public FieldConfigurationService(FieldConfigurationRepository fieldConfigurationRepository,
                                     FieldConfigurationMapper fieldConfigurationMapper) {
        this.fieldConfigurationRepository = fieldConfigurationRepository;
        this.fieldConfigurationMapper = fieldConfigurationMapper;
    }

    public FieldConfiguration create(FieldConfiguration fieldConfiguration) {
        if (fieldConfiguration == null) {
            throw new CustomException("Field configuration must not be null", HttpStatus.BAD_REQUEST);
        }
        return fieldConfigurationRepository.save(fieldConfiguration);
    }

    public FieldConfiguration update(Long id, FieldConfigurationPatchDTO fieldConfigurationPatchDTO) {
        FieldConfiguration savedFieldConfiguration = fieldConfigurationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Field configuration not found", HttpStatus.NOT_FOUND));

        fieldConfigurationMapper.updateFieldConfiguration(savedFieldConfiguration, fieldConfigurationPatchDTO);

        return fieldConfigurationRepository.save(savedFieldConfiguration);
    }

    @Transactional(readOnly = true)
    public Collection<FieldConfiguration> getAll() {
        return fieldConfigurationRepository.findAll();
    }

    public void delete(Long id) {
        if (!fieldConfigurationRepository.existsById(id)) {
            throw new CustomException("Field configuration not found", HttpStatus.NOT_FOUND);
        }
        fieldConfigurationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<FieldConfiguration> findById(Long id) {
        return fieldConfigurationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public FieldConfiguration getById(Long id) {
        return fieldConfigurationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Field configuration not found", HttpStatus.NOT_FOUND));
    }
}