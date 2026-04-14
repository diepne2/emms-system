package com.emms.backend.service;

import com.emms.backend.dto.request.RequestDTO;
import com.emms.backend.entity.Location;
import com.emms.backend.entity.Request;
import com.emms.backend.entity.RequestPortal;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.RequestMapper;
import com.emms.backend.repository.RequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final LocationService locationService;
    private final RequestPortalService requestPortalService;
    private final WorkOrderService workOrderService;

    public RequestService(
            RequestRepository requestRepository,
            RequestMapper requestMapper,
            LocationService locationService,
            RequestPortalService requestPortalService,
            WorkOrderService workOrderService
    ) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.locationService = locationService;
        this.requestPortalService = requestPortalService;
        this.workOrderService = workOrderService;
    }

    public Request create(RequestDTO dto) {
        if (dto == null) {
            throw new CustomException("Request data must not be null", HttpStatus.BAD_REQUEST);
        }

        Request entity = new Request();
        requestMapper.updateRequest(entity, dto);
        applyRelations(entity, dto);
        normalizeCancellation(entity);

        return requestRepository.save(entity);
    }

    public Request update(Long id, RequestDTO dto) {
        if (id == null) {
            throw new CustomException("Request id must not be null", HttpStatus.BAD_REQUEST);
        }

        if (dto == null) {
            throw new CustomException("Request data must not be null", HttpStatus.BAD_REQUEST);
        }

        Request entity = requestRepository.findById(id)
                .orElseThrow(() -> new CustomException("Request not found", HttpStatus.NOT_FOUND));

        requestMapper.updateRequest(entity, dto);
        applyRelations(entity, dto);
        normalizeCancellation(entity);

        return requestRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Request findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("Request id must not be null", HttpStatus.BAD_REQUEST);
        }

        return requestRepository.findById(id)
                .orElseThrow(() -> new CustomException("Request not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Request> findAll() {
        return requestRepository.findAll();
    }

    public void delete(Long id) {
        if (id == null) {
            throw new CustomException("Request id must not be null", HttpStatus.BAD_REQUEST);
        }

        Request entity = requestRepository.findById(id)
                .orElseThrow(() -> new CustomException("Request not found", HttpStatus.NOT_FOUND));

        requestRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public Collection<Request> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        validateDateRange(start, end);
        return requestRepository.findByCreatedAtBetween(start, end);
    }

    private void applyRelations(Request entity, RequestDTO dto) {
        if (dto.getLocationId() != null) {
            Location location = locationService.findEntityById(dto.getLocationId());
            entity.setLocation(location);
        }

        if (dto.getRequestPortalId() != null) {
            RequestPortal requestPortal = requestPortalService.findEntityById(dto.getRequestPortalId());
            entity.setRequestPortal(requestPortal);
        }

        if (dto.getWorkOrderId() != null) {
            WorkOrder workOrder = workOrderService.findEntityById(dto.getWorkOrderId());
            entity.setWorkOrder(workOrder);
        }
    }

    private void normalizeCancellation(Request entity) {
        if (!entity.isCancelled()) {
            entity.setCancellationReason(null);
            return;
        }

        if (entity.getCancellationReason() == null || entity.getCancellationReason().isBlank()) {
            entity.setCancellationReason("Request was cancelled");
        }
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new CustomException("Start date and end date must not be null", HttpStatus.BAD_REQUEST);
        }

        if (start.isAfter(end)) {
            throw new CustomException("Start date must be before or equal to end date", HttpStatus.BAD_REQUEST);
        }
    }
}