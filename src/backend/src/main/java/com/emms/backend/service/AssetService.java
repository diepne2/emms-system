package com.emms.backend.service;

import com.emms.backend.dto.asset.AssetPUTDTO;
import com.emms.backend.dto.asset.AssetShowDTO;
import com.emms.backend.dto.asset.AssetSummaryDTO;
import com.emms.backend.dto.importData.AssetImportDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.User;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.AssetMapper;
import com.emms.backend.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;

    // Giữ setter injection cho các dependency có thể bị vòng phụ thuộc
    private LocationService locationService;
    private LaborService laborService;
    private WorkOrderService workOrderService;

    private final FileService fileService;
    private final AssetCategoryService assetCategoryService;
    private final UserService userService;
    private final VendorService vendorService;
    private final NotificationService notificationService;
    private final PartService partService;
    private final AssetMapper assetMapper;
    private final AssetDowntimeService assetDowntimeService;
    private final MessageSource messageSource;

    @Autowired
    public void setDeps(@Lazy LocationService locationService,
                        @Lazy LaborService laborService,
                        @Lazy WorkOrderService workOrderService) {
        this.locationService = locationService;
        this.laborService = laborService;
        this.workOrderService = workOrderService;
    }

    public Asset create(Asset asset, User currentUser) {
        validateAssetForCreate(asset);
        normalize(asset);

        Optional<Asset> existing = findByNameIgnoreCaseSafe(asset.getName());
        if (existing.isPresent()) {
            throw new CustomException(
                    "Asset đã tồn tại với tên: " + asset.getName(),
                    HttpStatus.BAD_REQUEST
            );
        }

        return assetRepository.save(asset);
    }

    public Asset createFromDto(AssetPUTDTO dto, User currentUser) {
        if (dto == null) {
            throw new CustomException("Dữ liệu asset không được để trống", HttpStatus.BAD_REQUEST);
        }

        Asset asset = new Asset();
        applyDtoToEntity(asset, dto);

        return create(asset, currentUser);
    }

    public Asset update(Long id, AssetPUTDTO dto, User currentUser) {
        if (id == null) {
            throw new CustomException("Id asset không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException("Dữ liệu cập nhật không được để trống", HttpStatus.BAD_REQUEST);
        }

        Asset existing = getById(id);
        String oldName = existing.getName();

        applyDtoToEntity(existing, dto);
        validateAssetForUpdate(existing);

        if (existing.getName() != null
                && oldName != null
                && !existing.getName().equalsIgnoreCase(oldName)) {
            Optional<Asset> duplicate = findByNameIgnoreCaseSafe(existing.getName());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(existing.getId())) {
                throw new CustomException(
                        "Asset đã tồn tại với tên: " + existing.getName(),
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        return assetRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Asset getById(Long id) {
        if (id == null) {
            throw new CustomException("Id asset không được để trống", HttpStatus.BAD_REQUEST);
        }

        return assetRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy asset với id: " + id,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Transactional(readOnly = true)
    public Asset findEntityById(Long id) {
        return getById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Asset> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return assetRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Asset> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return findByNameIgnoreCaseSafe(name.trim());
    }

    @Transactional(readOnly = true)
    public List<Asset> findAll() {
        return assetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Collection<Asset> getAll() {
        return assetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AssetShowDTO getShowDtoById(Long id) {
        Asset asset = getById(id);
        return assetMapper.toShowDto(asset);
    }

    @Transactional(readOnly = true)
    public AssetSummaryDTO getSummaryDtoById(Long id) {
        Asset asset = getById(id);
        return assetMapper.toSummaryDto(asset);
    }

    @Transactional(readOnly = true)
    public List<AssetShowDTO> getAllShowDtos() {
        return assetRepository.findAll()
                .stream()
                .map(assetMapper::toShowDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssetSummaryDTO> getAllSummaryDtos() {
        return assetRepository.findAll()
                .stream()
                .map(assetMapper::toSummaryDto)
                .toList();
    }

    public void delete(Long id) {
        Asset asset = getById(id);
        assetRepository.delete(asset);
    }

    /**
     * Entity Asset hiện tại là text-only, không còn parentAsset relation,
     * nên tạm thời không kiểm tra asset con bằng foreign key.
     */
    @Transactional(readOnly = true)
    public boolean hasChildren(Long assetId) {
        return false;
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userService.findAll();
    }

    // =========================================================
    // IMPORT SUPPORT
    // =========================================================

    public AssetImportDTO[] orderAssets(List<AssetImportDTO> list) {
        if (list == null || list.isEmpty()) {
            return new AssetImportDTO[0];
        }

        Map<String, AssetImportDTO> assetsByName = new HashMap<>();
        for (AssetImportDTO dto : list) {
            if (dto == null) {
                continue;
            }

            String key = normalizeKey(dto.getName());
            if (key != null) {
                assetsByName.put(key, dto);
            }
        }

        List<AssetImportDTO> ordered = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();

        for (AssetImportDTO dto : list) {
            visitAsset(dto, assetsByName, visiting, visited, ordered);
        }

        return ordered.toArray(new AssetImportDTO[0]);
    }

    private void visitAsset(AssetImportDTO dto,
                            Map<String, AssetImportDTO> assetsByName,
                            Set<String> visiting,
                            Set<String> visited,
                            List<AssetImportDTO> ordered) {
        if (dto == null) {
            return;
        }

        String currentKey = normalizeKey(dto.getName());

        // Nếu asset không có name thì cứ add cuối theo thứ tự gốc
        if (currentKey == null) {
            ordered.add(dto);
            return;
        }

        if (visited.contains(currentKey)) {
            return;
        }

        if (visiting.contains(currentKey)) {
            throw new CustomException(
                    "Phát hiện vòng lặp parent asset tại asset: " + dto.getName(),
                    HttpStatus.BAD_REQUEST
            );
        }

        visiting.add(currentKey);

        String parentKey = normalizeKey(dto.getParentAssetName());
        if (parentKey != null) {
            AssetImportDTO parentDto = assetsByName.get(parentKey);
            if (parentDto != null) {
                visitAsset(parentDto, assetsByName, visiting, visited, ordered);
            }
        }

        visiting.remove(currentKey);
        visited.add(currentKey);
        ordered.add(dto);
    }

    public void setAssetFieldsFromImportDto(Asset entity,
                                            AssetImportDTO dto,
                                            Map<String, Asset> assetsByName) {
        if (entity == null) {
            throw new CustomException("Asset entity không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException("Asset import dto không được để trống", HttpStatus.BAD_REQUEST);
        }

        dto.validate();

        entity.setStatus(dto.getStatus());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setArea(dto.getArea());
        entity.setLocationName(dto.getLocationName());
        entity.setBarcode(dto.getBarCode());
        entity.setCategory(dto.getCategory());
        entity.setWarrantyExpiryDate(dto.getWarrantyExpirationDate());
        entity.setSerialNumber(dto.getSerialNumber());

        if (dto.getAssignedToEmails() != null && !dto.getAssignedToEmails().isEmpty()) {
            entity.setAssignedTo(dto.getAssignedToEmails().get(0));
        } else {
            entity.setAssignedTo(dto.getPrimaryUserEmail());
        }

        entity.setTeamNames(joinList(dto.getTeamsNames()));
        entity.setAssociatedParts(joinList(dto.getPartsNames()));
        entity.setVendor(joinList(dto.getVendorsNames()));
        entity.setContractor(joinList(dto.getCustomersNames()));
        entity.setAdditionalInfo(mapToString(dto.getAdditionalInfos()));

        String parentKey = normalizeKey(dto.getParentAssetName());
        if (parentKey != null && assetsByName != null) {
            Asset parent = assetsByName.get(parentKey);
            if (parent != null && parent.getName() != null) {
                entity.setParentAssetName(parent.getName());
            } else {
                entity.setParentAssetName(dto.getParentAssetName());
            }
        } else {
            entity.setParentAssetName(dto.getParentAssetName());
        }

        normalize(entity);
    }

    public List<Asset> saveAll(List<Asset> entities) {
        if (entities == null || entities.isEmpty()) {
            throw new CustomException("Danh sách asset không được để trống", HttpStatus.BAD_REQUEST);
        }

        Set<String> importNames = new HashSet<>();
        Set<String> importBarcodes = new HashSet<>();

        for (Asset entity : entities) {
            validateAssetForCreate(entity);
            normalize(entity);

            String nameKey = normalizeKey(entity.getName());
            if (nameKey != null && !importNames.add(nameKey)) {
                throw new CustomException(
                        "Trùng tên asset trong file import: " + entity.getName(),
                        HttpStatus.BAD_REQUEST
                );
            }

            String barcodeKey = normalizeKey(entity.getBarcode());
            if (barcodeKey != null && !importBarcodes.add(barcodeKey)) {
                throw new CustomException(
                        "Trùng barcode asset trong file import: " + entity.getBarcode(),
                        HttpStatus.BAD_REQUEST
                );
            }

            Optional<Asset> existingByName = findByNameIgnoreCaseSafe(entity.getName());
            if (existingByName.isPresent()) {
                throw new CustomException(
                        "Asset đã tồn tại với tên: " + entity.getName(),
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        return assetRepository.saveAll(entities);
    }

    // =========================================================
    // INTERNAL HELPERS
    // =========================================================

    private void validateAssetForCreate(Asset asset) {
        if (asset == null) {
            throw new CustomException("Asset không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (asset.getName() == null || asset.getName().trim().isEmpty()) {
            throw new CustomException("Tên asset không được để trống", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateAssetForUpdate(Asset asset) {
        if (asset == null) {
            throw new CustomException("Asset không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        if (asset.getName() == null || asset.getName().trim().isEmpty()) {
            throw new CustomException("Tên asset không được để trống", HttpStatus.BAD_REQUEST);
        }
    }

    private void applyDtoToEntity(Asset asset, AssetPUTDTO dto) {
        assetMapper.updateAsset(asset, dto);
        normalize(asset);
    }

    private void normalize(Asset asset) {
        if (asset == null) {
            return;
        }

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

    private Optional<Asset> findByNameIgnoreCaseSafe(String name) {
        try {
            return assetRepository.findByNameIgnoreCase(name);
        } catch (Exception ex) {
            return assetRepository.findAll()
                    .stream()
                    .filter(a -> a.getName() != null && a.getName().equalsIgnoreCase(name))
                    .findFirst();
        }
    }

    private String joinList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        List<String> normalized = values.stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .toList();

        if (normalized.isEmpty()) {
            return null;
        }

        return String.join(", ", normalized);
    }

    private String mapToString(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.toString();
    }

    private String normalizeKey(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, Locale.getDefault());
    }
}