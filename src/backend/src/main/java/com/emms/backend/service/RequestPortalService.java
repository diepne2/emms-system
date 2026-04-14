package com.emms.backend.service;

import com.emms.backend.dto.requestPortal.RequestPortalPatchDTO;
import com.emms.backend.dto.requestPortal.RequestPortalPostDTO;
import com.emms.backend.entity.RequestPortal;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.RequestPortalMapper;
import com.emms.backend.repository.RequestPortalRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RequestPortalService {

    private final RequestPortalRepository requestPortalRepository;
    private final RequestPortalMapper requestPortalMapper;

    public RequestPortalService(RequestPortalRepository requestPortalRepository,
                                RequestPortalMapper requestPortalMapper) {
        this.requestPortalRepository = requestPortalRepository;
        this.requestPortalMapper = requestPortalMapper;
    }

    public RequestPortal create(@Valid RequestPortalPostDTO requestPortalPostDTO) {
        RequestPortal requestPortal = requestPortalMapper.fromPostDto(requestPortalPostDTO);

        if (requestPortal == null) {
            throw new CustomException("Dữ liệu request portal không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        requestPortal.setUuid(UUID.randomUUID().toString());

        if (requestPortal.getFields() != null) {
            requestPortal.getFields().forEach(field -> field.setRequestPortal(requestPortal));
        }

        return requestPortalRepository.save(requestPortal);
    }

    @Transactional(readOnly = true)
    public List<RequestPortal> getAll() {
        return requestPortalRepository.findAll();
    }

    public void delete(Long id) {
        if (!requestPortalRepository.existsById(id)) {
            throw new CustomException("Không tìm thấy request portal", HttpStatus.NOT_FOUND);
        }
        requestPortalRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<RequestPortal> findById(Long id) {
        return requestPortalRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public RequestPortal getById(Long id) {
        return requestPortalRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy request portal", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public RequestPortal findEntityById(Long id) {
        return requestPortalRepository.findById(id)
                .orElseThrow(() -> new CustomException("Request portal not found", HttpStatus.NOT_FOUND));
    }

    public RequestPortal update(Long id, @Valid RequestPortalPatchDTO requestPortalPatchDTO) {
        RequestPortal savedRequestPortal = requestPortalRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy request portal", HttpStatus.NOT_FOUND));

        requestPortalMapper.updateRequestPortal(savedRequestPortal, requestPortalPatchDTO);

        if (savedRequestPortal.getFields() != null) {
            savedRequestPortal.getFields().forEach(field -> field.setRequestPortal(savedRequestPortal));
        }

        return requestPortalRepository.save(savedRequestPortal);
    }

    @Transactional(readOnly = true)
    public Optional<RequestPortal> findByUuid(String uuid) {
        return requestPortalRepository.findByUuid(uuid);
    }

    @Transactional(readOnly = true)
    public RequestPortal getByUuid(String uuid) {
        return requestPortalRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomException("Không tìm thấy request portal", HttpStatus.NOT_FOUND));
    }
}