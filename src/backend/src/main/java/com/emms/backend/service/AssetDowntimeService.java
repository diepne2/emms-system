package com.emms.backend.service;

import com.emms.backend.dto.asset.AssetDowntimeDTO;
import com.emms.backend.entity.AssetDowntime;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.AssetDowntimeMapper;
import com.emms.backend.repository.AssetDowntimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class AssetDowntimeService {

    private final AssetDowntimeRepository assetDowntimeRepository;
    private final AssetDowntimeMapper assetDowntimeMapper;

    public AssetDowntimeService(AssetDowntimeRepository assetDowntimeRepository,
                                AssetDowntimeMapper assetDowntimeMapper) {
        this.assetDowntimeRepository = assetDowntimeRepository;
        this.assetDowntimeMapper = assetDowntimeMapper;
    }

    public AssetDowntime create(AssetDowntime assetDowntime) {
        validateDowntime(assetDowntime);
        checkOverlapping(assetDowntime, null);
        return assetDowntimeRepository.save(assetDowntime);
    }

    public AssetDowntime save(AssetDowntime assetDowntime) {
        validateDowntime(assetDowntime);
        return assetDowntimeRepository.save(assetDowntime);
    }

    public AssetDowntime update(Long id, AssetDowntimeDTO dto) {
        if (id == null) {
            throw new CustomException("id không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException("AssetDowntimeDTO không được để trống", HttpStatus.BAD_REQUEST);
        }

        AssetDowntime savedAssetDowntime = assetDowntimeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Asset downtime not found", HttpStatus.NOT_FOUND));

        assetDowntimeMapper.updateAssetDowntimeFromDto(dto, savedAssetDowntime);

        validateDowntime(savedAssetDowntime);
        checkOverlapping(savedAssetDowntime, id);

        return assetDowntimeRepository.save(savedAssetDowntime);
    }

    @Transactional(readOnly = true)
    public Collection<AssetDowntime> getAll() {
        return assetDowntimeRepository.findAll();
    }

    public void delete(Long id) {
        if (id == null) {
            throw new CustomException("id không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (!assetDowntimeRepository.existsById(id)) {
            throw new CustomException("Asset downtime not found", HttpStatus.NOT_FOUND);
        }
        assetDowntimeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AssetDowntime getById(Long id) {
        if (id == null) {
            throw new CustomException("id không được để trống", HttpStatus.BAD_REQUEST);
        }

        return assetDowntimeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Asset downtime not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<AssetDowntime> findByAsset(Long assetId) {
        if (assetId == null) {
            throw new CustomException("assetId không được để trống", HttpStatus.BAD_REQUEST);
        }
        return assetDowntimeRepository.findByAsset_Id(assetId);
    }

    public long getDowntimesMeanTimeHours(Collection<AssetDowntime> downtimes) {
        if (downtimes == null || downtimes.size() < 2) {
            return 0L;
        }

        List<AssetDowntime> sorted = downtimes.stream()
                .filter(d -> d.getStartsOn() != null)
                .sorted(Comparator.comparing(AssetDowntime::getStartsOn))
                .toList();

        if (sorted.size() < 2) {
            return 0L;
        }

        LocalDateTime first = sorted.get(0).getStartsOn();
        LocalDateTime last = sorted.get(sorted.size() - 1).getStartsOn();

        long hours = Duration.between(first, last).toHours();
        return hours / (sorted.size() - 1);
    }

    private void validateDowntime(AssetDowntime assetDowntime) {
        if (assetDowntime == null) {
            throw new CustomException("Asset downtime is required", HttpStatus.BAD_REQUEST);
        }

        if (assetDowntime.getAsset() == null || assetDowntime.getAsset().getId() == null) {
            throw new CustomException("Asset is required", HttpStatus.BAD_REQUEST);
        }

        if (assetDowntime.getStartsOn() == null) {
            throw new CustomException("startsOn is required", HttpStatus.BAD_REQUEST);
        }

        if (assetDowntime.getEndsOn() != null &&
                assetDowntime.getEndsOn().isBefore(assetDowntime.getStartsOn())) {
            throw new CustomException("endsOn must be greater than or equal to startsOn",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void checkOverlapping(AssetDowntime assetDowntime, Long currentDowntimeId) {
        Long assetId = assetDowntime.getAsset().getId();
        List<AssetDowntime> assetDowntimes = assetDowntimeRepository.findByAsset_Id(assetId);

        LocalDateTime startedOn = assetDowntime.getStartsOn();
        LocalDateTime endedOn = assetDowntime.getEndsOn();

        for (AssetDowntime existing : assetDowntimes) {
            if (currentDowntimeId != null && existing.getId().equals(currentDowntimeId)) {
                continue;
            }

            LocalDateTime existingStart = existing.getStartsOn();
            LocalDateTime existingEnd = existing.getEndsOn();

            LocalDateTime endA = endedOn != null ? endedOn : LocalDateTime.MAX;
            LocalDateTime endB = existingEnd != null ? existingEnd : LocalDateTime.MAX;

            boolean overlap = !startedOn.isAfter(endB) && !endA.isBefore(existingStart);

            if (overlap) {
                throw new CustomException("Downtime periods cannot overlap", HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }
}