package com.emms.backend.service;

import com.emms.backend.advancedsearch.SearchCriteria;
import com.emms.backend.dto.asset.AssetPUTDTO;
import com.emms.backend.dto.asset.AssetShowDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.enums.AssetStatus;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.AssetMapper;
import com.emms.backend.repository.AssetRepository;
import com.emms.backend.repository.MeterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private final MeterRepository meterRepository;
    private final AssetMapper assetMapper;
    private final MeterService meterService;

    public Asset create(AssetPUTDTO dto) {
        if (dto == null) {
            throw new CustomException("Dữ liệu asset không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        Asset asset = new Asset();
        assetMapper.updateAsset(asset, dto);

        normalize(asset);
        validateForSave(asset);
        validateDuplicateForCreate(asset);

        Asset saved = assetRepository.save(asset);
        meterService.createDefaultMetersForAsset(saved);

        return saved;
    }

    public Asset update(Long id, AssetPUTDTO dto) {
        if (id == null) {
            throw new CustomException("Id asset không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        if (dto == null) {
            throw new CustomException("Dữ liệu asset không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        Asset asset = getById(id);

        assetMapper.updateAsset(asset, dto);
        normalize(asset);
        validateForSave(asset);
        validateDuplicateForUpdate(asset);

        return assetRepository.save(asset);
    }

    @Transactional(readOnly = true)
    public Asset getById(Long id) {
        if (id == null) {
            throw new CustomException("Id asset không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        return assetRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy asset", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public AssetShowDTO getShowDtoById(Long id) {
        return assetMapper.toShowDto(getById(id));
    }

    @Transactional(readOnly = true)
    public List<AssetShowDTO> getAll() {
        return assetRepository.findAll()
                .stream()
                .filter(Objects::nonNull)
                .map(assetMapper::toShowDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssetShowDTO> getChildren(Long id) {
        Asset parent = getById(id);
        String parentName = trim(parent.getName());

        if (parentName == null || parentName.isBlank()) {
            return List.of();
        }

        return assetRepository.findAll()
                .stream()
                .filter(Objects::nonNull)
                .filter(asset -> asset.getId() != null && !asset.getId().equals(parent.getId()))
                .filter(asset -> equalsIgnoreCase(trim(asset.getParentAssetName()), parentName))
                .map(assetMapper::toShowDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AssetShowDTO> search(SearchCriteria criteria) {
        SearchCriteria safeCriteria = criteria == null ? new SearchCriteria() : criteria;

        int pageNum = safePageNum(safeCriteria.getPageNum());
        int pageSize = safePageSize(safeCriteria.getPageSize());

        String sortField = safeSortField(safeCriteria.getSortField());
        Sort.Direction direction = safeDirection(safeCriteria.getDirection());

        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(direction, sortField));

        List<Asset> filtered = new ArrayList<>(assetRepository.findAll());

        filtered.removeIf(asset -> {
            String parentAssetName = trim(asset.getParentAssetName());
            return parentAssetName != null && !parentAssetName.isBlank();
        });

        if (safeCriteria.getFilterFields() != null) {
            safeCriteria.getFilterFields().forEach(filter -> {
                if (filter == null || filter.getField() == null) {
                    return;
                }

                String field = trim(filter.getField());
                String operation = trim(filter.getOperation());
                String value = filter.getValue() == null ? null : String.valueOf(filter.getValue()).trim();

                if (field == null || operation == null || value == null || value.isBlank()) {
                    return;
                }

                switch (field) {
                    case "name" -> {
                        if ("cn".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset ->
                                    !containsIgnoreCase(asset.getName(), value)
                                            && !containsIgnoreCase(asset.getDescription(), value));
                        } else if ("eq".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset -> !equalsIgnoreCase(asset.getName(), value));
                        }
                    }

                    case "barcode" -> {
                        if ("cn".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset -> !containsIgnoreCase(asset.getBarcode(), value));
                        } else if ("eq".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset -> !equalsIgnoreCase(asset.getBarcode(), value));
                        }
                    }

                    case "serialNumber" -> {
                        if ("cn".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset -> !containsIgnoreCase(asset.getSerialNumber(), value));
                        } else if ("eq".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset -> !equalsIgnoreCase(asset.getSerialNumber(), value));
                        }
                    }

                    case "status" -> {
                        if ("eq".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset ->
                                    asset.getStatus() == null
                                            || !asset.getStatus().name().equalsIgnoreCase(value));
                        }
                    }

                    case "locationName" -> {
                        if ("cn".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset -> !containsIgnoreCase(asset.getLocationName(), value));
                        } else if ("eq".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset -> !equalsIgnoreCase(asset.getLocationName(), value));
                        }
                    }

                    case "warrantyExpiryDate" -> {
                        LocalDate date = parseDate(value);
                        if (date == null) {
                            return;
                        }

                        if ("eq".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset -> !Objects.equals(asset.getWarrantyExpiryDate(), date));
                        } else if ("ge".equalsIgnoreCase(operation) || "gte".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset ->
                                    asset.getWarrantyExpiryDate() == null
                                            || asset.getWarrantyExpiryDate().isBefore(date));
                        } else if ("le".equalsIgnoreCase(operation) || "lte".equalsIgnoreCase(operation)) {
                            filtered.removeIf(asset ->
                                    asset.getWarrantyExpiryDate() == null
                                            || asset.getWarrantyExpiryDate().isAfter(date));
                        }
                    }

                    default -> {
                    }
                }
            });
        }

        filtered.sort(buildComparator(sortField, direction));

        int start = Math.min(pageNum * pageSize, filtered.size());
        int end = Math.min(start + pageSize, filtered.size());

        List<AssetShowDTO> content = filtered.subList(start, end)
                .stream()
                .map(assetMapper::toShowDto)
                .toList();

        return new PageImpl<>(content, pageable, filtered.size());
    }

    public Asset decommission(Long id) {
        Asset asset = getById(id);

        if (asset.getStatus() != null && asset.getStatus().isDecommissioned()) {
            return asset;
        }

        asset.setStatus(AssetStatus.DECOMMISSIONED);
        return assetRepository.save(asset);
    }

    public void delete(Long id) {
        Asset asset = getById(id);

        if (asset.getStatus() == null || !asset.getStatus().isDecommissioned()) {
            throw new CustomException("Phải NGỪNG trước khi xóa", HttpStatus.CONFLICT);
        }

        boolean hasChildren = assetRepository.findAll()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(item ->
                        item.getId() != null
                                && !item.getId().equals(asset.getId())
                                && equalsIgnoreCase(trim(item.getParentAssetName()), trim(asset.getName()))
                );

        if (hasChildren) {
            throw new CustomException("Không thể xóa vì asset vẫn còn thiết bị con", HttpStatus.CONFLICT);
        }

        if (meterRepository.existsByAsset_Id(id)) {
            throw new CustomException("Không thể xóa vì có meter", HttpStatus.CONFLICT);
        }

        assetRepository.delete(asset);
    }

    private void validateForSave(Asset asset) {
        if (isBlank(asset.getName())) {
            throw new CustomException("Tên asset không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (isBlank(asset.getBarcode())) {
            throw new CustomException("Mã code không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (isBlank(asset.getSerialNumber())) {
            throw new CustomException("Serial number không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (asset.getWarrantyExpiryDate() == null) {
            throw new CustomException("Ngày không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (isBlank(asset.getLocationName())) {
            throw new CustomException("Location không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (asset.getStatus() == null) {
            asset.setStatus(AssetStatus.OPERATIONAL);
        }
    }

    private void validateDuplicateForCreate(Asset asset) {
        validateDuplicateName(asset, null);
        validateDuplicateBarcode(asset, null);
        validateDuplicateSerialNumber(asset, null);
    }

    private void validateDuplicateForUpdate(Asset asset) {
        validateDuplicateName(asset, asset.getId());
        validateDuplicateBarcode(asset, asset.getId());
        validateDuplicateSerialNumber(asset, asset.getId());
    }

    private void validateDuplicateName(Asset asset, Long currentId) {
        String name = trim(asset.getName());

        if (isBlank(name)) {
            return;
        }

        boolean exists = currentId == null
                ? assetRepository.existsByNameIgnoreCase(name)
                : assetRepository.existsByNameIgnoreCaseAndIdNot(name, currentId);
        if (exists) {
            throw new CustomException("Tên thiết bị đã tồn tại: " + name, HttpStatus.BAD_REQUEST);
        }


    }

    private void validateDuplicateBarcode(Asset asset, Long currentId) {
        String barcode = trim(asset.getBarcode());

        if (isBlank(barcode)) {
            return;
        }

        boolean exists = currentId == null
            ? assetRepository.existsByBarcodeIgnoreCase(barcode)
            : assetRepository.existsByBarcodeIgnoreCaseAndIdNot(barcode, currentId);
        if (exists) {
            throw new CustomException("Mã / Barcode đã tồn tại: " + barcode, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateDuplicateSerialNumber(Asset asset, Long currentId) {
        String serialNumber = trim(asset.getSerialNumber());

        if (isBlank(serialNumber)) {
            return;
        }

        boolean exists = currentId == null
            ? assetRepository.existsBySerialNumberIgnoreCase(serialNumber)
            : assetRepository.existsBySerialNumberIgnoreCaseAndIdNot(serialNumber, currentId);
            
        if (exists) {
            throw new CustomException("Serial Number đã tồn tại: " + serialNumber, HttpStatus.BAD_REQUEST);
        }
    }

    private void normalize(Asset asset) {
        if (asset.getName() != null) asset.setName(asset.getName().trim());
        if (asset.getDescription() != null) asset.setDescription(asset.getDescription().trim());
        if (asset.getArea() != null) asset.setArea(asset.getArea().trim());
        if (asset.getParentAssetName() != null) asset.setParentAssetName(asset.getParentAssetName().trim());
        if (asset.getLocationName() != null) asset.setLocationName(asset.getLocationName().trim());
        if (asset.getBarcode() != null) asset.setBarcode(asset.getBarcode().trim());
        if (asset.getCategory() != null) asset.setCategory(asset.getCategory().trim());
        if (asset.getAssignedTo() != null) asset.setAssignedTo(asset.getAssignedTo().trim());
        if (asset.getAdditionalInfo() != null) asset.setAdditionalInfo(asset.getAdditionalInfo().trim());
        if (asset.getSerialNumber() != null) asset.setSerialNumber(asset.getSerialNumber().trim());
        if (asset.getTeamNames() != null) asset.setTeamNames(asset.getTeamNames().trim());
        if (asset.getAssociatedParts() != null) asset.setAssociatedParts(asset.getAssociatedParts().trim());
        if (asset.getVendor() != null) asset.setVendor(asset.getVendor().trim());
        if (asset.getContractor() != null) asset.setContractor(asset.getContractor().trim());
    }

    private int safePageNum(int pageNum) {
        return Math.max(pageNum, 0);
    }

    private int safePageSize(int pageSize) {
        if (pageSize <= 0) {
            return 5;
        }

        return Math.min(pageSize, 100);
    }

    private String safeSortField(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            return "id";
        }

        return switch (sortField) {
            case "id", "name", "barcode", "serialNumber", "status", "locationName", "category", "warrantyExpiryDate" -> sortField;
            default -> "id";
        };
    }

    private Sort.Direction safeDirection(Sort.Direction direction) {
        return direction == null ? Sort.Direction.DESC : direction;
    }

    private Comparator<Asset> buildComparator(String sortField, Sort.Direction direction) {
        Comparator<Asset> comparator = switch (sortField) {
            case "name" -> Comparator.comparing(Asset::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "barcode" -> Comparator.comparing(Asset::getBarcode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "serialNumber" -> Comparator.comparing(Asset::getSerialNumber, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "status" -> Comparator.comparing(
                    asset -> asset.getStatus() == null ? null : asset.getStatus().name(),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
            case "locationName" -> Comparator.comparing(Asset::getLocationName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "category" -> Comparator.comparing(Asset::getCategory, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "warrantyExpiryDate" -> Comparator.comparing(Asset::getWarrantyExpiryDate, Comparator.nullsLast(LocalDate::compareTo));
            default -> Comparator.comparing(Asset::getId, Comparator.nullsLast(Long::compareTo));
        };

        return direction == Sort.Direction.ASC ? comparator : comparator.reversed();
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (source == null || keyword == null) {
            return false;
        }

        return source.toLowerCase().contains(keyword.toLowerCase());
    }

    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        return a.equalsIgnoreCase(b);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}