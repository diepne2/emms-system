package com.emms.backend.service;

import com.emms.backend.dto.location.LocationDTO;
import com.emms.backend.dto.location.LocationShowDTO;
import com.emms.backend.dto.location.LocationSummaryDTO;
import com.emms.backend.entity.Location;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.LocationMapper;
import com.emms.backend.repository.LocationRepository;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final EntityManager em;

    public LocationService(
            LocationRepository locationRepository,
            LocationMapper locationMapper,
            EntityManager em
    ) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
        this.em = em;
    }

    public LocationShowDTO create(LocationDTO dto) {
        if (dto == null) {
            throw new CustomException("Dữ liệu vị trí không được để trống", HttpStatus.BAD_REQUEST);
        }

        Location entity = locationMapper.fromDto(dto);

        validateEntity(entity);
        validateDuplicateNameForCreate(entity.getName());

        Location saved = locationRepository.saveAndFlush(entity);
        em.refresh(saved);

        return locationMapper.toShowDto(saved);
    }

    public LocationShowDTO update(Long id, LocationDTO dto) {
        if (id == null) {
            throw new CustomException("ID vị trí không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (dto == null) {
            throw new CustomException("Dữ liệu vị trí không được để trống", HttpStatus.BAD_REQUEST);
        }

        Location existing = findEntityById(id);

        locationMapper.updateLocationFromDto(dto, existing);

        validateEntity(existing);
        validateDuplicateNameForUpdate(existing.getName(), id);

        Location saved = locationRepository.saveAndFlush(existing);
        em.refresh(saved);

        return locationMapper.toShowDto(saved);
    }

    public void delete(Long id) {
        if (id == null) {
            throw new CustomException("ID vị trí không được để trống", HttpStatus.BAD_REQUEST);
        }

        Location existing = findEntityById(id);

        try {
            locationRepository.delete(existing);
            locationRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new CustomException(
                    "Không thể xóa vị trí vì đang được sử dụng bởi thiết bị, meter hoặc dữ liệu liên quan.",
                    HttpStatus.CONFLICT
            );
        }
    }

    @Transactional(readOnly = true)
    public LocationShowDTO getById(Long id) {
        return locationMapper.toShowDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Optional<Location> findById(Long id) {
        if (id == null) {
            throw new CustomException("ID vị trí không được để trống", HttpStatus.BAD_REQUEST);
        }

        return locationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Location findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("ID vị trí không được để trống", HttpStatus.BAD_REQUEST);
        }

        return locationRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy vị trí",
                        HttpStatus.NOT_FOUND
                ));
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

    @Transactional(readOnly = true)
    public List<LocationShowDTO> search(String keyword) {
        String q = keyword == null ? "" : keyword.trim();

        if (q.isBlank()) {
            return getAll();
        }

        return locationRepository.searchByKeyword(q)
                .stream()
                .map(locationMapper::toShowDto)
                .toList();
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

    @Transactional(readOnly = true)
    public List<Location> getAllEntities() {
        return locationRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    private void validateEntity(Location location) {
        if (location == null) {
            throw new CustomException("Dữ liệu vị trí không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (location.getName() == null || location.getName().trim().isBlank()) {
            throw new CustomException("Tên vị trí không được để trống", HttpStatus.BAD_REQUEST);
        }

        location.setName(location.getName().trim());

        if (location.getAddress() != null) {
            location.setAddress(location.getAddress().trim());
        }

        if (location.getParentLocation() != null) {
            location.setParentLocation(location.getParentLocation().trim());
        }
    }

    private void validateDuplicateNameForCreate(String name) {
        String normalized = trim(name);

        if (normalized != null && locationRepository.existsByNameIgnoreCase(normalized)) {
            throw new CustomException("Tên vị trí đã tồn tại", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateDuplicateNameForUpdate(String name, Long id) {
        String normalized = trim(name);

        if (normalized != null && locationRepository.existsByNameIgnoreCaseAndIdNot(normalized, id)) {
            throw new CustomException("Tên vị trí đã tồn tại", HttpStatus.BAD_REQUEST);
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}