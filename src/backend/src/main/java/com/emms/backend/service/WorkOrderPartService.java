package com.emms.backend.service;

import com.emms.backend.dto.part.WorkOrderPartShowDTO;
import com.emms.backend.entity.Part;
import com.emms.backend.entity.PartTransaction;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.WorkOrderPart;
import com.emms.backend.repository.PartRepository;
import com.emms.backend.repository.PartTransactionRepository;
import com.emms.backend.repository.WorkOrderPartRepository;
import com.emms.backend.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WorkOrderPartService {

    private final WorkOrderRepository workOrderRepository;
    private final PartRepository partRepository;
    private final WorkOrderPartRepository workOrderPartRepository;
    private final PartTransactionRepository partTransactionRepository;

    public WorkOrderPartService(
            WorkOrderRepository workOrderRepository,
            PartRepository partRepository,
            WorkOrderPartRepository workOrderPartRepository,
            PartTransactionRepository partTransactionRepository
    ) {
        this.workOrderRepository = workOrderRepository;
        this.partRepository = partRepository;
        this.workOrderPartRepository = workOrderPartRepository;
        this.partTransactionRepository = partTransactionRepository;
    }

    public void usePart(Long workOrderId, Long partId, Integer qty) {
        if (workOrderId == null) {
            throw new RuntimeException("workOrderId không được để trống");
        }

        if (partId == null) {
            throw new RuntimeException("partId không được để trống");
        }

        if (qty == null || qty <= 0) {
            throw new RuntimeException("Số lượng vật tư phải lớn hơn 0");
        }

        WorkOrder wo = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new RuntimeException("Work order không tồn tại: " + workOrderId));

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Part không tồn tại: " + partId));

        int beforeQty = part.getQuantity() == null ? 0 : part.getQuantity();

        if (beforeQty < qty) {
            throw new RuntimeException("Không đủ tồn kho. Tồn hiện tại: " + beforeQty);
        }

        int afterQty = beforeQty - qty;

        part.setQuantity(afterQty);
        partRepository.save(part);

        WorkOrderPart wop = new WorkOrderPart();
        wop.setWorkOrder(wo);
        wop.setPart(part);
        wop.setQuantityUsed(qty);
        wop.setCost(part.getCost() != null ? part.getCost() : BigDecimal.ZERO);
        wop.setUsedAt(LocalDateTime.now());

        workOrderPartRepository.save(wop);

        savePartTransaction(
                part.getId(),
                wo.getId(),
                "USE_FOR_WORK_ORDER",
                qty,
                beforeQty,
                afterQty,
                "Xuất vật tư cho Work Order #" + wo.getId()
        );

        recalcCost(wo);
        workOrderRepository.save(wo);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderPartShowDTO> getByWorkOrderDto(Long woId) {
        List<WorkOrderPart> parts = workOrderPartRepository.findByWorkOrder_Id(woId);
        return parts.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<WorkOrderPart> getByWorkOrder(Long woId) {
        return workOrderPartRepository.findByWorkOrder_Id(woId);
    }

    private WorkOrderPartShowDTO toDto(WorkOrderPart entity) {
        WorkOrderPartShowDTO dto = new WorkOrderPartShowDTO();

        dto.setId(entity.getId());

        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
        }

        if (entity.getPart() != null) {
            dto.setPartId(entity.getPart().getId());
            dto.setPartName(entity.getPart().getName());
            dto.setPartNumber(entity.getPart().getPartNumber());
            dto.setBarcode(entity.getPart().getBarcode());
            dto.setCategory(entity.getPart().getCategory());
            dto.setCurrentStock(entity.getPart().getQuantity());
        }

        dto.setQuantityUsed(entity.getQuantityUsed());
        dto.setUnitCost(entity.getCost());
        dto.setUsedAt(entity.getUsedAt());

        BigDecimal unitCost = entity.getCost() != null
                ? entity.getCost()
                : BigDecimal.ZERO;

        int quantityUsed = entity.getQuantityUsed() != null
                ? entity.getQuantityUsed()
                : 0;

        dto.setLineTotal(unitCost.multiply(BigDecimal.valueOf(quantityUsed)));

        return dto;
    }

    private void recalcCost(WorkOrder wo) {
        List<WorkOrderPart> parts =
                workOrderPartRepository.findByWorkOrder_Id(wo.getId());

        BigDecimal total = parts.stream()
                .map(p -> {
                    BigDecimal cost = p.getCost() != null
                            ? p.getCost()
                            : BigDecimal.ZERO;

                    int qty = p.getQuantityUsed() != null
                            ? p.getQuantityUsed()
                            : 0;

                    return cost.multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        wo.setTotalCost(total);
    }

    private void savePartTransaction(
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

        partTransactionRepository.save(transaction);
    }
}