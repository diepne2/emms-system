package com.emms.backend.service;

import com.emms.backend.dto.asset.AssetDowntimeDTO;
import com.emms.backend.dto.asset.AssetDowntimeShowDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.AssetDowntime;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.AssetDowntimeMapper;
import com.emms.backend.repository.AssetDowntimeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class AssetDowntimeService {

    private final AssetDowntimeRepository assetDowntimeRepository;
    private final AssetDowntimeMapper assetDowntimeMapper;
    private final AssetService assetService;
    private final WorkOrderService workOrderService;

    public AssetDowntimeService(AssetDowntimeRepository assetDowntimeRepository,
                                AssetDowntimeMapper assetDowntimeMapper,
                                AssetService assetService,
                                WorkOrderService workOrderService) {
        this.assetDowntimeRepository = assetDowntimeRepository;
        this.assetDowntimeMapper = assetDowntimeMapper;
        this.assetService = assetService;
        this.workOrderService = workOrderService;
    }

    public AssetDowntimeShowDTO create(AssetDowntimeDTO dto) {
        validateDto(dto);

        AssetDowntime entity = new AssetDowntime();
        applyDtoToEntity(dto, entity);

        validateEntity(entity);
        checkOverlapping(entity, null);

        AssetDowntime saved = assetDowntimeRepository.save(entity);
        return assetDowntimeMapper.toShowDto(saved);
    }

    public AssetDowntimeShowDTO update(Long id, AssetDowntimeDTO dto) {
        if (id == null) {
            throw new CustomException("id không được để trống", HttpStatus.BAD_REQUEST);
        }
        validateDto(dto);

        AssetDowntime existing = getEntityById(id);
        applyDtoToEntity(dto, existing);

        validateEntity(existing);
        checkOverlapping(existing, id);

        AssetDowntime saved = assetDowntimeRepository.save(existing);
        return assetDowntimeMapper.toShowDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AssetDowntimeShowDTO> getAll() {
        return assetDowntimeRepository.findAll(
                        Sort.by(Sort.Direction.DESC, "startsOn")
                ).stream()
                .map(assetDowntimeMapper::toShowDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssetDowntimeShowDTO getById(Long id) {
        return assetDowntimeMapper.toShowDto(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<AssetDowntimeShowDTO> findByAsset(Long assetId) {
        if (assetId == null) {
            throw new CustomException("assetId không được để trống", HttpStatus.BAD_REQUEST);
        }

        return assetDowntimeRepository.findByAsset_Id(assetId).stream()
                .sorted(Comparator.comparing(AssetDowntime::getStartsOn).reversed())
                .map(assetDowntimeMapper::toShowDto)
                .toList();
    }

    public void delete(Long id) {
        if (id == null) {
            throw new CustomException("id không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (!assetDowntimeRepository.existsById(id)) {
            throw new CustomException("Không tìm thấy thời gian ngừng hoạt động của thiết bị", HttpStatus.NOT_FOUND);
        }

        assetDowntimeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AssetDowntime getEntityById(Long id) {
        if (id == null) {
            throw new CustomException("id không được để trống", HttpStatus.BAD_REQUEST);
        }

        return assetDowntimeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy thời gian ngừng hoạt động của thiết bị", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
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

    private void applyDtoToEntity(AssetDowntimeDTO dto, AssetDowntime entity) {
        Asset asset = assetService.getById(dto.getAssetId());
        WorkOrder workOrder = resolveWorkOrder(dto.getWorkOrderId());
        AssetDowntime.DowntimeReason reason = resolveReason(dto.getReason());

        entity.setAsset(asset);
        entity.setWorkOrder(workOrder);
        entity.setReason(reason);
        entity.setStartsOn(dto.getStartsOn());
        entity.setEndsOn(dto.getEndsOn());
        entity.setNote(dto.getNote());
    }

    private WorkOrder resolveWorkOrder(Long workOrderId) {
        if (workOrderId == null) {
            return null;
        }
        return workOrderService.findEntityById(workOrderId);
    }

    private AssetDowntime.DowntimeReason resolveReason(String rawReason) {
        if (rawReason == null || rawReason.isBlank()) {
            return AssetDowntime.DowntimeReason.BREAKDOWN;
        }

        String normalized = rawReason.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');

        try {
            return AssetDowntime.DowntimeReason.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new CustomException(
                    "reason không hợp lệ. Chấp nhận: BREAKDOWN, MAINTENANCE, POWER_FAILURE, CALIBRATION, OTHER",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateDto(AssetDowntimeDTO dto) {
        if (dto == null) {
            throw new CustomException("AssetDowntimeDTO không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getAssetId() == null) {
            throw new CustomException("assetId không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getStartsOn() == null) {
            throw new CustomException("startsOn không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getEndsOn() != null && dto.getEndsOn().isBefore(dto.getStartsOn())) {
            throw new CustomException("endsOn phải >= startsOn", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateEntity(AssetDowntime entity) {
        if (entity == null) {
            throw new CustomException("Thời gian ngừng hoạt động của thiết bị là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (entity.getAsset() == null || entity.getAsset().getId() == null) {
            throw new CustomException("Thiết bị là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (entity.getStartsOn() == null) {
            throw new CustomException("startsOn là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (entity.getEndsOn() != null && entity.getEndsOn().isBefore(entity.getStartsOn())) {
            throw new CustomException("Thời điểm kết thúc phải lớn hơn hoặc bằng thời điểm bắt đầu.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void checkOverlapping(AssetDowntime assetDowntime, Long currentDowntimeId) {
        Long assetId = assetDowntime.getAsset().getId();
        List<AssetDowntime> assetDowntimes = assetDowntimeRepository.findByAsset_Id(assetId);

        LocalDateTime startA = assetDowntime.getStartsOn();
        LocalDateTime endA = assetDowntime.getEndsOn() != null
                ? assetDowntime.getEndsOn()
                : LocalDateTime.MAX;

        for (AssetDowntime existing : assetDowntimes) {
            if (currentDowntimeId != null && currentDowntimeId.equals(existing.getId())) {
                continue;
            }

            LocalDateTime startB = existing.getStartsOn();
            LocalDateTime endB = existing.getEndsOn() != null
                    ? existing.getEndsOn()
                    : LocalDateTime.MAX;


            boolean overlap = startA.isBefore(endB) && startB.isBefore(endA);

            if (overlap) {
                throw new CustomException("Thời gian ngừng hoạt động của thiết bị bị trùng lặp", HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }
}