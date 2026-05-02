package com.emms.backend.service;

import com.emms.backend.dto.part.PartPatchDTO;
import com.emms.backend.dto.part.PartShowDTO;
import com.emms.backend.dto.part.PartSummaryDTO;
import com.emms.backend.entity.Part;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.PartMapper;
import com.emms.backend.repository.PartRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PartService {

    private final PartRepository partRepository;
    private final PartMapper partMapper;

    public PartService(PartRepository partRepository, PartMapper partMapper) {
        this.partRepository = partRepository;
        this.partMapper = partMapper;
    }

    public Part create(Part part) {
        if (part == null) {
            throw new CustomException("Part không được để trống", HttpStatus.BAD_REQUEST);
        }

        normalizePart(part);
        validatePart(part);

        if (part.getQuantity() == null) {
            part.setQuantity(0);
        }

        return partRepository.save(part);
    }

    public PartShowDTO createAndReturnDto(Part part) {
        return partMapper.toShowDto(create(part));
    }

    public Part update(Long partId, PartPatchDTO dto) {
        if (partId == null) {
            throw new CustomException("partId không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException("PartPatchDTO không được để trống", HttpStatus.BAD_REQUEST);
        }

        Part savedPart = partRepository.findById(partId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy part với id: " + partId,
                        HttpStatus.NOT_FOUND
                ));

        partMapper.updatePartFromDto(dto, savedPart);
        normalizePart(savedPart);
        validatePart(savedPart);

        return partRepository.save(savedPart);
    }

    public PartShowDTO updateAndReturnDto(Long partId, PartPatchDTO dto) {
        return partMapper.toShowDto(update(partId, dto));
    }

    @Transactional(readOnly = true)
    public Collection<Part> getAll() {
        return partRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PartSummaryDTO> getAllSummary() {
        return partRepository.findAll()
                .stream()
                .map(partMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Part getById(Long partId) {
        if (partId == null) {
            throw new CustomException("partId không được để trống", HttpStatus.BAD_REQUEST);
        }

        return partRepository.findById(partId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy part với id: " + partId,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Transactional(readOnly = true)
    public PartShowDTO getShowDtoById(Long partId) {
        return partMapper.toShowDto(getById(partId));
    }

    public void delete(Long partId) {
        if (partId == null) {
            throw new CustomException("partId không được để trống", HttpStatus.BAD_REQUEST);
        }

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy part với id: " + partId,
                        HttpStatus.NOT_FOUND
                ));

        partRepository.delete(part);
    }

    public List<Part> saveAll(List<Part> entities) {
        if (entities == null || entities.isEmpty()) {
            throw new CustomException("Danh sách part không được để trống", HttpStatus.BAD_REQUEST);
        }

        for (Part entity : entities) {
            normalizePart(entity);

            if (entity.getQuantity() == null) {
                entity.setQuantity(0);
            }
            validatePart(entity);
        }

        return partRepository.saveAll(entities);
    }

    public Part increaseStock(Long partId, Integer amount) {
        if (partId == null) {
            throw new CustomException("partId không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (amount == null || amount <= 0) {
            throw new CustomException("Số lượng tăng phải > 0", HttpStatus.BAD_REQUEST);
        }

        Part part = getById(partId);
        int currentQty = part.getQuantity() == null ? 0 : part.getQuantity();
        part.setQuantity(currentQty + amount);

        return partRepository.save(part);
    }

    public Part decreaseStock(Long partId, Integer amount) {
        if (partId == null) {
            throw new CustomException("partId không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (amount == null || amount <= 0) {
            throw new CustomException("Số lượng giảm phải > 0", HttpStatus.BAD_REQUEST);
        }

        Part part = getById(partId);
        int currentQty = part.getQuantity() == null ? 0 : part.getQuantity();

        if (currentQty < amount) {
            throw new CustomException("Không đủ tồn kho để trừ", HttpStatus.BAD_REQUEST);
        }

        part.setQuantity(currentQty - amount);
        return partRepository.save(part);
    }

   

    private void validatePart(Part part) {
        if (part == null) {
            throw new CustomException("Part không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (isBlank(part.getName())) {
            throw new CustomException("Tên vật tư không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (part.getCost() != null) {
            validateMoney(part.getCost(), "cost");
        }

        if (part.getLastPrice() != null) {
            validateMoney(part.getLastPrice(), "lastPrice");
        }

        if (part.getQuantity() != null && part.getQuantity() < 0) {
            throw new CustomException("quantity không được âm", HttpStatus.BAD_REQUEST);
        }
    }

    private void normalizePart(Part part) {
        if (part == null) {
            return;
        }

        if (part.getName() != null) {
            part.setName(part.getName().trim());
        }
        if (part.getCategory() != null) {
            part.setCategory(part.getCategory().trim());
        }
        if (part.getBarcode() != null) {
            part.setBarcode(part.getBarcode().trim());
        }
        if (part.getDescription() != null) {
            part.setDescription(part.getDescription().trim());
        }
        if (part.getLocationName() != null) {
            part.setLocationName(part.getLocationName().trim());
        }
        if (part.getVendor() != null) {
            part.setVendor(part.getVendor().trim());
        }
        if (part.getAssignedTo() != null) {
            part.setAssignedTo(part.getAssignedTo().trim().toLowerCase());
        }
    }

    private void validateMoney(BigDecimal value, String fieldName) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(fieldName + " không được âm", HttpStatus.BAD_REQUEST);
        }
    }

    private String joinDistinct(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (!isBlank(value)) {
                normalized.add(value.trim());
            }
        }

        return normalized.isEmpty() ? null : String.join(", ", normalized);
    }

    private String joinDistinctLower(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (!isBlank(value)) {
                normalized.add(value.trim().toLowerCase());
            }
        }

        return normalized.isEmpty() ? null : String.join(", ", normalized);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}