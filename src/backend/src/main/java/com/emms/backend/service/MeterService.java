package com.emms.backend.service;

import com.emms.backend.dto.importData.MeterImportDTO;
import com.emms.backend.dto.meter.MeterDTO;
import com.emms.backend.dto.meter.MeterShowDTO;
import com.emms.backend.dto.meter.MeterSummaryDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.AssetDocument;
import com.emms.backend.entity.Location;
import com.emms.backend.entity.Meter;
import com.emms.backend.entity.MeterCategory;
import com.emms.backend.entity.User;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.MeterMapper;
import com.emms.backend.repository.MeterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MeterService {

    private final MeterRepository meterRepository;
    private final MeterMapper meterMapper;
    private final MeterCategoryService meterCategoryService;
    private final FileService fileService;
    private final LocationService locationService;
    private final AssetService assetService;
    private final UserService userService;
    private final ReadingService readingService;

    public MeterService(
            MeterRepository meterRepository,
            MeterMapper meterMapper,
            MeterCategoryService meterCategoryService,
            FileService fileService,
            LocationService locationService,
            AssetService assetService,
            UserService userService,
            ReadingService readingService
    ) {
        this.meterRepository = meterRepository;
        this.meterMapper = meterMapper;
        this.meterCategoryService = meterCategoryService;
        this.fileService = fileService;
        this.locationService = locationService;
        this.assetService = assetService;
        this.userService = userService;
        this.readingService = readingService;
    }

    public MeterShowDTO create(MeterDTO dto) {
        validateCreate(dto);

        Meter entity = new Meter();
        meterMapper.updateMeter(entity, dto);

        Asset asset = assetService.findEntityById(dto.getAssetId());
        entity.setAsset(asset);

        applyRelations(entity, dto);

        Meter saved = meterRepository.save(entity);
        return meterMapper.toShowDto(saved, readingService);
    }

    public MeterShowDTO update(Long id, MeterDTO dto) {
        if (id == null) {
            throw new CustomException("Meter id must not be null", HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException("Meter data must not be null", HttpStatus.BAD_REQUEST);
        }

        Meter entity = meterRepository.findById(id)
                .orElseThrow(() -> new CustomException("Meter not found", HttpStatus.NOT_FOUND));

        meterMapper.updateMeter(entity, dto);

        if (dto.getAssetId() != null) {
            Asset asset = assetService.findEntityById(dto.getAssetId());
            entity.setAsset(asset);
        }

        applyRelations(entity, dto);

        Meter saved = meterRepository.save(entity);
        return meterMapper.toShowDto(saved, readingService);
    }

    @Transactional(readOnly = true)
    public MeterShowDTO getById(Long id) {
        Meter entity = findEntityById(id);
        return meterMapper.toShowDto(entity, readingService);
    }

    @Transactional(readOnly = true)
    public Meter findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("Meter id must not be null", HttpStatus.BAD_REQUEST);
        }

        return meterRepository.findById(id)
                .orElseThrow(() -> new CustomException("Meter not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<MeterShowDTO> getAll() {
        return meterRepository.findAll()
                .stream()
                .map(meter -> meterMapper.toShowDto(meter, readingService))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MeterSummaryDTO> getAllSummary() {
        return meterRepository.findAll()
                .stream()
                .map(meterMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        Meter entity = findEntityById(id);
        meterRepository.delete(entity);
    }

    private void validateCreate(MeterDTO dto) {
        if (dto == null) {
            throw new CustomException("Meter data must not be null", HttpStatus.BAD_REQUEST);
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new CustomException("Meter name must not be blank", HttpStatus.BAD_REQUEST);
        }
        if (dto.getUpdateFrequency() == null || dto.getUpdateFrequency() < 1) {
            throw new CustomException("Update frequency must be >= 1", HttpStatus.BAD_REQUEST);
        }
        if (dto.getAssetId() == null) {
            throw new CustomException("Asset id must not be null", HttpStatus.BAD_REQUEST);
        }
    }

    private void applyRelations(Meter entity, MeterDTO dto) {
        if (dto == null) {
            return;
        }

        if (dto.getMeterCategoryId() != null) {
            MeterCategory meterCategory = meterCategoryService.findEntityById(dto.getMeterCategoryId());
            entity.setMeterCategory(meterCategory);
        }

        if (dto.getImageId() != null) {
            
        }

        if (dto.getLocationId() != null) {
            Location location = locationService.findEntityById(dto.getLocationId());
            entity.setLocation(location);
        }

        if (dto.getUserIds() != null) {
            List<User> users = dto.getUserIds().stream()
                    .map(userService::findEntityById)
                    .collect(Collectors.toList());
            entity.setUsers(users);
        }
    }

    public void importMeter(Meter entity, MeterImportDTO dto) {
        throw new UnsupportedOperationException("Unimplemented method 'importMeter'");
    }

    public void save(List<Meter> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }
}