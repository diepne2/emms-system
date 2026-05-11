package com.emms.backend.service;

import com.emms.backend.dto.part.InventoryCountItemDTO;
import com.emms.backend.dto.part.PartPatchDTO;
import com.emms.backend.dto.part.PartShowDTO;
import com.emms.backend.dto.part.PartSummaryDTO;
import com.emms.backend.entity.InventoryCount;
import com.emms.backend.entity.InventoryCountItem;
import com.emms.backend.entity.InventoryMonthlyClosing;
import com.emms.backend.entity.Part;
import com.emms.backend.entity.PartTransaction;
import com.emms.backend.entity.enums.InventoryCountStatus;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.PartMapper;
import com.emms.backend.repository.InventoryCountItemRepository;
import com.emms.backend.repository.InventoryCountRepository;
import com.emms.backend.repository.InventoryMonthlyClosingRepository;
import com.emms.backend.repository.PartRepository;
import com.emms.backend.repository.PartTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final PartTransactionRepository partTransactionRepository;
    private final InventoryCountRepository inventoryCountRepository;
    private final InventoryCountItemRepository inventoryCountItemRepository;
    private final InventoryMonthlyClosingRepository inventoryMonthlyClosingRepository;

    public PartService(
            PartRepository partRepository,
            PartMapper partMapper,
            PartTransactionRepository partTransactionRepository,
            InventoryCountRepository inventoryCountRepository,
            InventoryCountItemRepository inventoryCountItemRepository,
            InventoryMonthlyClosingRepository inventoryMonthlyClosingRepository
    ) {
        this.partRepository = partRepository;
        this.partMapper = partMapper;
        this.partTransactionRepository = partTransactionRepository;
        this.inventoryCountRepository = inventoryCountRepository;
        this.inventoryCountItemRepository = inventoryCountItemRepository;
        this.inventoryMonthlyClosingRepository = inventoryMonthlyClosingRepository;
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
        return importStock(partId, amount, "Nhập kho");
    }

    public Part importStock(Long partId, Integer amount, String note) {
        validateAmount(amount, "Số lượng nhập kho");

        Part part = getById(partId);

        int beforeQty = safeQty(part.getQuantity());
        int afterQty = beforeQty + amount;

        part.setQuantity(afterQty);
        Part saved = partRepository.save(part);

        saveTransaction(
                partId,
                null,
                "IMPORT",
                amount,
                beforeQty,
                afterQty,
                note
        );

        return saved;
    }

    public Part usePartForWorkOrder(Long workOrderId, Long partId, Integer amount) {
        if (workOrderId == null) {
            throw new CustomException("workOrderId không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (partId == null) {
            throw new CustomException("partId không được để trống", HttpStatus.BAD_REQUEST);
        }

        validateAmount(amount, "Số lượng vật tư sử dụng");

        Part part = getById(partId);

        int beforeQty = safeQty(part.getQuantity());

        if (beforeQty < amount) {
            throw new CustomException(
                    "Không đủ tồn kho để xuất vật tư cho Work Order",
                    HttpStatus.BAD_REQUEST
            );
        }

        int afterQty = beforeQty - amount;

        part.setQuantity(afterQty);
        Part saved = partRepository.save(part);

        saveTransaction(
                partId,
                workOrderId,
                "USE_FOR_WORK_ORDER",
                amount,
                beforeQty,
                afterQty,
                "Xuất vật tư cho Work Order #" + workOrderId
        );

        return saved;
    }

    public InventoryCount createInventoryCount(Integer year, Integer month, String note) {
        validateYearMonth(year, month);

        boolean closed = inventoryMonthlyClosingRepository
                .existsByYearAndMonthAndStatus(year, month, "CLOSED");

        if (closed) {
            throw new CustomException(
                    "Kỳ kho tháng " + month + "/" + year + " đã chốt, không thể tạo kiểm kê",
                    HttpStatus.BAD_REQUEST
            );
        }

        InventoryCount count = new InventoryCount();
        count.setCode("KK-" + year + "-" + String.format("%02d", month) + "-" + System.currentTimeMillis());
        count.setYear(year);
        count.setMonth(month);
        count.setStatus(InventoryCountStatus.DRAFT);
        count.setNote(note);

        return inventoryCountRepository.save(count);
    }

    public InventoryCountItem addInventoryCountItem(
            Long inventoryCountId,
            InventoryCountItemDTO dto
    ) {
        if (dto == null) {
            throw new CustomException("Dữ liệu kiểm kê không được để trống", HttpStatus.BAD_REQUEST);
        }

        InventoryCount count = inventoryCountRepository.findById(inventoryCountId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy phiếu kiểm kê",
                        HttpStatus.NOT_FOUND
                ));

        if (count.getStatus() != InventoryCountStatus.DRAFT) {
            throw new CustomException(
                    "Phiếu kiểm kê đã được duyệt, không thể sửa",
                    HttpStatus.BAD_REQUEST
            );
        }

        Part part = getById(dto.getPartId());

        int systemQty = safeQty(part.getQuantity());
        int actualQty = dto.getActualQuantity() == null ? 0 : dto.getActualQuantity();

        if (actualQty < 0) {
            throw new CustomException("Số lượng thực tế không được âm", HttpStatus.BAD_REQUEST);
        }

        int diff = actualQty - systemQty;

        InventoryCountItem item = new InventoryCountItem();
        item.setInventoryCountId(inventoryCountId);
        item.setPartId(part.getId());
        item.setSystemQuantity(systemQty);
        item.setActualQuantity(actualQty);
        item.setDifferenceQuantity(diff);
        item.setNote(dto.getNote());

        return inventoryCountItemRepository.save(item);
    }

    public InventoryCount confirmInventoryCount(Long inventoryCountId) {
        InventoryCount count = inventoryCountRepository.findById(inventoryCountId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy phiếu kiểm kê",
                        HttpStatus.NOT_FOUND
                ));

        if (count.getStatus() != InventoryCountStatus.DRAFT) {
            throw new CustomException(
                    "Phiếu kiểm kê đã được duyệt, không thể sửa",
                    HttpStatus.BAD_REQUEST
            );
        }

        boolean closed = inventoryMonthlyClosingRepository
                .existsByYearAndMonthAndStatus(count.getYear(), count.getMonth(), "CLOSED");

        if (closed) {
            throw new CustomException(
                    "Kỳ kho đã chốt, không thể duyệt kiểm kê",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<InventoryCountItem> items =
                inventoryCountItemRepository.findByInventoryCountId(inventoryCountId);

        if (items == null || items.isEmpty()) {
            throw new CustomException(
                    "Phiếu kiểm kê chưa có vật tư",
                    HttpStatus.BAD_REQUEST
            );
        }

        for (InventoryCountItem item : items) {
            Part part = getById(item.getPartId());

            int beforeQty = safeQty(part.getQuantity());
            int afterQty = item.getActualQuantity();

            part.setQuantity(afterQty);
            partRepository.save(part);

            if (beforeQty != afterQty) {
                saveTransaction(
                        part.getId(),
                        null,
                        "ADJUSTMENT",
                        Math.abs(afterQty - beforeQty),
                        beforeQty,
                        afterQty,
                        "Điều chỉnh sau kiểm kê " + count.getCode()
                );
            }
        }

        count.setStatus(InventoryCountStatus.CONFIRMED);
        count.setConfirmedAt(LocalDateTime.now());

        return inventoryCountRepository.save(count);
    }

    public void deleteInventoryCount(Long inventoryCountId) {
        if (inventoryCountId == null) {
            throw new CustomException(
                    "inventoryCountId không được để trống",
                    HttpStatus.BAD_REQUEST
            );
        }

        InventoryCount count = inventoryCountRepository.findById(inventoryCountId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy phiếu kiểm kê",
                        HttpStatus.NOT_FOUND
                ));

        if (count.getStatus() == InventoryCountStatus.CONFIRMED) {
            throw new CustomException(
                    "Không thể xóa phiếu kiểm kê đã duyệt",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<InventoryCountItem> items =
                inventoryCountItemRepository.findByInventoryCountId(inventoryCountId);

        if (items != null && !items.isEmpty()) {
            inventoryCountItemRepository.deleteAll(items);
        }

        inventoryCountRepository.delete(count);
    }

    public InventoryMonthlyClosing closeMonth(Integer year, Integer month, String note) {
        validateYearMonth(year, month);

        boolean existed = inventoryMonthlyClosingRepository
                .existsByYearAndMonthAndStatus(year, month, "CLOSED");

        if (existed) {
            throw new CustomException(
                    "Kỳ kho tháng " + month + "/" + year + " đã được chốt",
                    HttpStatus.BAD_REQUEST
            );
        }

        boolean hasConfirmedInventoryCount =
                inventoryCountRepository.existsByYearAndMonthAndStatus(
                        year,
                        month,
                        InventoryCountStatus.CONFIRMED
                );

        if (!hasConfirmedInventoryCount) {
            throw new CustomException(
                    "Cần có phiếu kiểm kê đã duyệt trước khi chốt sổ kho",
                    HttpStatus.BAD_REQUEST
            );
        }

        InventoryMonthlyClosing closing = new InventoryMonthlyClosing();
        closing.setYear(year);
        closing.setMonth(month);
        closing.setStatus("CLOSED");
        closing.setClosedAt(LocalDateTime.now());
        closing.setNote(note);

        return inventoryMonthlyClosingRepository.save(closing);
    }

    public InventoryMonthlyClosing reopenMonthlyClosing(Long closingId) {
        if (closingId == null) {
            throw new CustomException(
                    "closingId không được để trống",
                    HttpStatus.BAD_REQUEST
            );
        }

        InventoryMonthlyClosing closing =
                inventoryMonthlyClosingRepository.findById(closingId)
                        .orElseThrow(() -> new CustomException(
                                "Không tìm thấy kỳ chốt kho",
                                HttpStatus.NOT_FOUND
                        ));

        if (!"CLOSED".equalsIgnoreCase(closing.getStatus())) {
            throw new CustomException(
                    "Chỉ có thể mở lại kỳ kho đang ở trạng thái CLOSED",
                    HttpStatus.BAD_REQUEST
            );
        }

        closing.setStatus("REOPENED");

        return inventoryMonthlyClosingRepository.save(closing);
    }

    @Transactional(readOnly = true)
    public List<PartTransaction> getTransactionsByPart(Long partId) {
        if (partId == null) {
            throw new CustomException("partId không được để trống", HttpStatus.BAD_REQUEST);
        }

        return partTransactionRepository.findByPartIdOrderByCreatedAtDesc(partId);
    }

    @Transactional(readOnly = true)
    public List<PartTransaction> getAllTransactions(String keyword, String type) {
        String normalizedKeyword = isBlank(keyword) ? null : keyword.trim();
        String normalizedType = isBlank(type) ? null : type.trim();

        return partTransactionRepository.searchTransactions(
                normalizedKeyword,
                normalizedType
        );
    }

    private void saveTransaction(
            Long partId,
            Long workOrderId,
            String type,
            Integer quantity,
            Integer beforeQty,
            Integer afterQty,
            String note
    ) {
        PartTransaction transaction = new PartTransaction();
        transaction.setPartId(partId);
        transaction.setWorkOrderId(workOrderId);
        transaction.setType(type);
        transaction.setQuantity(quantity);
        transaction.setBeforeQuantity(beforeQty);
        transaction.setAfterQuantity(afterQty);
        transaction.setNote(note);
        transaction.setCreatedBy(getCurrentUsername());

        partTransactionRepository.save(transaction);
    }

    private String getCurrentUsername() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "SYSTEM";
        }

        String username = authentication.getName();

        if (username == null || username.trim().isEmpty() || "anonymousUser".equals(username)) {
            return "SYSTEM";
        }

        return username;
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

    private void validateAmount(Integer amount, String fieldName) {
        if (amount == null || amount <= 0) {
            throw new CustomException(fieldName + " phải > 0", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateYearMonth(Integer year, Integer month) {
        if (year == null || year < 2000) {
            throw new CustomException("Năm không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        if (month == null || month < 1 || month > 12) {
            throw new CustomException("Tháng không hợp lệ", HttpStatus.BAD_REQUEST);
        }
    }

    private int safeQty(Integer quantity) {
        return quantity == null ? 0 : quantity;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
}