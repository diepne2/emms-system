package com.emms.backend.service;

import com.emms.backend.dto.meter.MeterDTO;
import com.emms.backend.dto.meter.MeterShowDTO;
import com.emms.backend.dto.meter.MeterSummaryDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.Meter;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.MeterMapper;
import com.emms.backend.repository.MeterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MeterService {

    private static final int DEFAULT_UPDATE_FREQUENCY = 1;
    private static final String DEFAULT_METER_NAME = "Runtime";
    private static final String DEFAULT_METER_UNIT = "hours";

    private final MeterRepository meterRepository;
    private final MeterMapper meterMapper;

    public MeterService(MeterRepository meterRepository, MeterMapper meterMapper) {
        this.meterRepository = meterRepository;
        this.meterMapper = meterMapper;
    }


    @Transactional(readOnly = true)
    public Meter findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("Id meter không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        return meterRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy meter", HttpStatus.NOT_FOUND));
    }

    public Meter create(MeterDTO dto) {
        if (dto == null) {
            throw new CustomException("Dữ liệu meter không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        Meter meter = new Meter();
        meterMapper.updateEntityFromDto(dto, meter);

        applyDefaultValues(meter);
        normalize(meter);
        validateForSave(meter);

        return meterRepository.save(meter);
    }

    public Meter update(Long id, MeterDTO dto) {
        if (id == null) {
            throw new CustomException("Id meter không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        if (dto == null) {
            throw new CustomException("Dữ liệu meter không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        Meter existing = findEntityById(id);
        meterMapper.updateEntityFromDto(dto, existing);

        applyDefaultValues(existing);
        normalize(existing);
        validateForSave(existing);

        return meterRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Meter getById(Long id) {
        return findEntityById(id);
    }

    @Transactional(readOnly = true)
    public MeterShowDTO getShowDtoById(Long id) {
        return meterMapper.toShowDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<MeterShowDTO> getAllShowDtos() {
        return meterRepository.findAll()
                .stream()
                .map(meterMapper::toShowDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MeterSummaryDTO> getAllSummaryDtos() {
        return meterRepository.findAll()
                .stream()
                .map(meterMapper::toSummaryDto)
                .toList();
    }

    public void delete(Long id) {
        Meter meter = findEntityById(id);
        meterRepository.delete(meter);
    }

    public void save(List<Meter> meters) {
        if (meters == null || meters.isEmpty()) {
            return;
        }

        meters.forEach(meter -> {
            applyDefaultValues(meter);
            normalize(meter);
            validateForSave(meter);
        });

        meterRepository.saveAll(meters);
    }


    public void importMeter(Meter meter, Object dto) {
        if (meter == null) {
            throw new CustomException("Meter import không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        applyDefaultValues(meter);
        normalize(meter);
        validateForSave(meter);

        meterRepository.save(meter);
    }

    public void createDefaultMetersForAsset(Asset asset) {
        if (asset == null || asset.getId() == null) {
            return;
        }

        if (meterRepository.existsByAsset_Id(asset.getId())) {
            return;
        }

        Meter runtime = new Meter();
        runtime.setName(DEFAULT_METER_NAME);
        runtime.setUnit(DEFAULT_METER_UNIT);
        runtime.setAsset(asset);


        runtime.setUpdateFrequency(DEFAULT_UPDATE_FREQUENCY);

        applyDefaultValues(runtime);
        normalize(runtime);
        validateForSave(runtime);

        meterRepository.save(runtime);
    }



    private void applyDefaultValues(Meter meter) {
        if (meter == null) {
            return;
        }

        if (meter.getUpdateFrequency() == null || meter.getUpdateFrequency() < 1) {
            meter.setUpdateFrequency(DEFAULT_UPDATE_FREQUENCY);
        }

        if (meter.getUsers() == null) {
            meter.setUsers(List.of());
        }

        if (meter.getName() == null || meter.getName().isBlank()) {
            meter.setName(DEFAULT_METER_NAME);
        }

        if (meter.getUnit() == null || meter.getUnit().isBlank()) {
            meter.setUnit(DEFAULT_METER_UNIT);
        }
    }

    private void normalize(Meter meter) {
        if (meter == null) {
            return;
        }

        if (meter.getName() != null) {
            meter.setName(meter.getName().trim());
        }

        if (meter.getUnit() != null) {
            meter.setUnit(meter.getUnit().trim());
        }

        if (meter.getUpdateFrequency() == null || meter.getUpdateFrequency() < 1) {
            meter.setUpdateFrequency(DEFAULT_UPDATE_FREQUENCY);
        }
    }

    private void validateForSave(Meter meter) {
        if (meter == null) {
            throw new CustomException("Meter không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        if (meter.getName() == null || meter.getName().isBlank()) {
            throw new CustomException("Tên meter không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (meter.getAsset() == null || meter.getAsset().getId() == null) {
            throw new CustomException("Meter phải gắn với asset hợp lệ", HttpStatus.BAD_REQUEST);
        }

        if (meter.getUpdateFrequency() == null || meter.getUpdateFrequency() < 1) {
            throw new CustomException("updateFrequency phải >= 1", HttpStatus.BAD_REQUEST);
        }
    }
}