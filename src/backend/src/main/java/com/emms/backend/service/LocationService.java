package com.emms.backend.service;

import com.emms.backend.dto.location.LocationDTO;
import com.emms.backend.dto.location.LocationShowDTO;
import com.emms.backend.dto.location.LocationSummaryDTO;
import com.emms.backend.entity.Location;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.LocationMapper;
import com.emms.backend.repository.LocationRepository;
import jakarta.persistence.EntityManager;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.*;

@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final MessageSource messageSource;
    private final EntityManager em;

    public LocationService(
            LocationRepository locationRepository,
            LocationMapper locationMapper,
            MessageSource messageSource,
            EntityManager em
    ) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
        this.messageSource = messageSource;
        this.em = em;
    }


    public Location create(Location location) {
        validateEntity(location);
        validateDuplicateNameForCreate(location.getName());

        Location saved = locationRepository.saveAndFlush(location);
        em.refresh(saved);
        return saved;
    }

    public Location update(Long id, Location location) {
        if (id == null) {
            throw new CustomException(getMessage("location.id.required", "ID địa điểm không được để trống"), HttpStatus.BAD_REQUEST);
        }
        if (location == null) {
            throw new CustomException(getMessage("location.data.required", "Dữ liệu địa điểm không được để trống"), HttpStatus.BAD_REQUEST);
        }

        Location existing = findEntityById(id);
        applyEntityPatch(existing, location);
        validateEntity(existing);
        validateDuplicateNameForUpdate(existing.getName(), id);

        Location saved = locationRepository.saveAndFlush(existing);
        em.refresh(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Location> getAllEntities() {
        return locationRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public void delete(Long id) {
        if (id == null) {
            throw new CustomException(getMessage("location.id.required", "ID địa điểm không được để trống"), HttpStatus.BAD_REQUEST);
        }

        Location existing = findEntityById(id);
        locationRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public Optional<Location> findById(Long id) {
        if (id == null) {
            throw new CustomException(getMessage("location.id.required", "ID địa điểm không được để trống"), HttpStatus.BAD_REQUEST);
        }
        return locationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Location findEntityById(Long id) {
        if (id == null) {
            throw new CustomException(getMessage("location.id.required", "ID địa điểm không được để trống"), HttpStatus.BAD_REQUEST);
        }

        return locationRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        getMessage("location.notfound", "Location not found"),
                        HttpStatus.NOT_FOUND
                ));
    }

    public Location save(Location location) {
        validateEntity(location);

        if (location.getId() == null) {
            validateDuplicateNameForCreate(location.getName());
        } else {
            validateDuplicateNameForUpdate(location.getName(), location.getId());
        }

        return locationRepository.save(location);
    }

    public void save(List<Location> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        for (Location entity : entities) {
            validateEntity(entity);
        }

        locationRepository.saveAll(entities);
    }


    public LocationShowDTO create(LocationDTO dto) {
        if (dto == null) {
            throw new CustomException(getMessage("location.data.required", "Dữ liệu địa điểm không được để trống"), HttpStatus.BAD_REQUEST);
        }

        Location entity = locationMapper.fromDto(dto);
        Location saved = create(entity);
        return locationMapper.toShowDto(saved);
    }

    public LocationShowDTO update(Long locationId, LocationDTO dto) {
        if (locationId == null) {
            throw new CustomException(getMessage("location.id.required", "ID địa điểm không được để trống"), HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException(getMessage("location.data.required", "Dữ liệu địa điểm không được để trống"), HttpStatus.BAD_REQUEST);
        }

        Location existing = findEntityById(locationId);
        locationMapper.updateLocationFromDto(dto, existing);
        validateEntity(existing);
        validateDuplicateNameForUpdate(existing.getName(), locationId);

        Location saved = locationRepository.saveAndFlush(existing);
        em.refresh(saved);
        return locationMapper.toShowDto(saved);
    }

    @Transactional(readOnly = true)
    public LocationShowDTO getById(Long locationId) {
        return locationMapper.toShowDto(findEntityById(locationId));
    }

    @Transactional(readOnly = true)
    public List<LocationShowDTO> getAll() {
        return locationRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(locationMapper::toShowDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LocationSummaryDTO> getAllSummary() {
        return locationRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(locationMapper::toSummaryDto)
                .toList();
    }

    private void validateEntity(Location location) {
        if (location == null) {
            throw new CustomException(getMessage("location.data.required", "Dữ liệu địa điểm không được để trống"), HttpStatus.BAD_REQUEST);
        }
        if (location.getName() == null || location.getName().isBlank()) {
            throw new CustomException(getMessage("location.name.required", "Tên địa điểm không được để trống"), HttpStatus.BAD_REQUEST);
        }
    }

    private void validateDuplicateNameForCreate(String name) {
        String normalized = trim(name);
        if (normalized != null && locationRepository.existsByNameIgnoreCase(normalized)) {
            throw new CustomException(
                    getMessage("location.name.exists", "Tên địa điểm đã tồn tại"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateDuplicateNameForUpdate(String name, Long id) {
        String normalized = trim(name);
        if (normalized != null && locationRepository.existsByNameIgnoreCaseAndIdNot(normalized, id)) {
            throw new CustomException(
                    getMessage("location.name.exists", "Tên địa điểm đã tồn tại"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void applyEntityPatch(Location existing, Location request) {
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getAddress() != null) {
            existing.setAddress(request.getAddress());
        }
        if (request.getParentLocation() != null) {
            existing.setParentLocation(request.getParentLocation());
        }
        if (request.getVendors() != null) {
            existing.setVendors(request.getVendors());
        }
        if (request.getContractors() != null) {
            existing.setContractors(request.getContractors());
        }
    }

    private String invokeStringGetter(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value == null ? null : value.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String getMessage(String code, String defaultMessage) {
        try {
            return messageSource.getMessage(code, null, Locale.getDefault());
        } catch (Exception ex) {
            return defaultMessage;
        }
    }
}