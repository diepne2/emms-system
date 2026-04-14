package com.emms.backend.service;

import com.emms.backend.dto.importData.LocationImportDTO;
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

    // =========================
    // ENTITY-BASED CRUD
    // =========================
    public Location create(Location location) {
        validateEntity(location);
        validateDuplicateNameForCreate(location.getName());

        Location saved = locationRepository.saveAndFlush(location);
        em.refresh(saved);
        return saved;
    }

    public Location update(Long id, Location location) {
        if (id == null) {
            throw new CustomException(getMessage("location.id.required", "Location id must not be null"), HttpStatus.BAD_REQUEST);
        }
        if (location == null) {
            throw new CustomException(getMessage("location.data.required", "Location data must not be null"), HttpStatus.BAD_REQUEST);
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
            throw new CustomException(getMessage("location.id.required", "Location id must not be null"), HttpStatus.BAD_REQUEST);
        }

        Location existing = findEntityById(id);
        locationRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public Optional<Location> findById(Long id) {
        if (id == null) {
            throw new CustomException(getMessage("location.id.required", "Location id must not be null"), HttpStatus.BAD_REQUEST);
        }
        return locationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Location findEntityById(Long id) {
        if (id == null) {
            throw new CustomException(getMessage("location.id.required", "Location id must not be null"), HttpStatus.BAD_REQUEST);
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

    // =========================
    // DTO-BASED CRUD
    // =========================
    public LocationShowDTO create(LocationDTO dto) {
        if (dto == null) {
            throw new CustomException(getMessage("location.data.required", "Location data must not be null"), HttpStatus.BAD_REQUEST);
        }

        Location entity = locationMapper.fromDto(dto);
        Location saved = create(entity);
        return locationMapper.toShowDto(saved);
    }

    public LocationShowDTO update(Long locationId, LocationDTO dto) {
        if (locationId == null) {
            throw new CustomException(getMessage("location.id.required", "Location id must not be null"), HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException(getMessage("location.data.required", "Location data must not be null"), HttpStatus.BAD_REQUEST);
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

    // =========================
    // IMPORT SUPPORT
    // =========================
    public LocationImportDTO[] orderLocations(List<LocationImportDTO> list) {
        if (list == null || list.isEmpty()) {
            return new LocationImportDTO[0];
        }

        List<LocationImportDTO> result = new ArrayList<>(list);
        result.sort(Comparator.comparingInt(dto -> getDepth(dto, list)));
        return result.toArray(new LocationImportDTO[0]);
    }

    private int getDepth(LocationImportDTO dto, List<LocationImportDTO> all) {
        if (dto == null) {
            return 0;
        }

        String parentName = trim(extractParentLocation(dto));
        if (parentName == null) {
            return 0;
        }

        int depth = 0;
        Set<String> visited = new HashSet<>();
        String currentParent = parentName;

        while (currentParent != null && visited.add(currentParent.toLowerCase())) {
            depth++;

            String finalCurrentParent = currentParent;
            LocationImportDTO parentDto = all.stream()
                    .filter(item -> finalCurrentParent.equalsIgnoreCase(trim(extractName(item))))
                    .findFirst()
                    .orElse(null);

            if (parentDto == null) {
                break;
            }

            currentParent = trim(extractParentLocation(parentDto));
        }

        return depth;
    }

    public void setLocationFieldsFromImportDto(
            Location entity,
            LocationImportDTO dto,
            Map<String, Location> locationsByName
    ) {
        if (entity == null) {
            throw new CustomException(getMessage("location.entity.required", "Location entity must not be null"), HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException(getMessage("location.import.required", "Location import data must not be null"), HttpStatus.BAD_REQUEST);
        }

        entity.setName(extractName(dto));
        entity.setAddress(extractAddress(dto));
        entity.setParentLocation(extractParentLocation(dto));
        entity.setVendors(extractVendors(dto));
        entity.setContractors(extractContractors(dto));

        validateEntity(entity);

        if (locationsByName != null && entity.getName() != null) {
            locationsByName.put(entity.getName().trim().toLowerCase(), entity);
        }
    }

    // =========================
    // INTERNAL HELPERS
    // =========================
    private void validateEntity(Location location) {
        if (location == null) {
            throw new CustomException(getMessage("location.data.required", "Location data must not be null"), HttpStatus.BAD_REQUEST);
        }
        if (location.getName() == null || location.getName().isBlank()) {
            throw new CustomException(getMessage("location.name.required", "Location name must not be blank"), HttpStatus.BAD_REQUEST);
        }
    }

    private void validateDuplicateNameForCreate(String name) {
        String normalized = trim(name);
        if (normalized != null && locationRepository.existsByNameIgnoreCase(normalized)) {
            throw new CustomException(
                    getMessage("location.name.exists", "Location name already exists"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateDuplicateNameForUpdate(String name, Long id) {
        String normalized = trim(name);
        if (normalized != null && locationRepository.existsByNameIgnoreCaseAndIdNot(normalized, id)) {
            throw new CustomException(
                    getMessage("location.name.exists", "Location name already exists"),
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

    private String extractName(LocationImportDTO dto) {
        return trim(invokeStringGetter(dto, "getName"));
    }

    private String extractAddress(LocationImportDTO dto) {
        return trim(invokeStringGetter(dto, "getAddress"));
    }

    private String extractParentLocation(LocationImportDTO dto) {
        return trim(invokeStringGetter(dto, "getParentLocation"));
    }

    private String extractVendors(LocationImportDTO dto) {
        return trim(invokeStringGetter(dto, "getVendors"));
    }

    private String extractContractors(LocationImportDTO dto) {
        return trim(invokeStringGetter(dto, "getContractors"));
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