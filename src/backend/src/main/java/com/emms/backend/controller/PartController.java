package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.part.InventoryCountItemDTO;
import com.emms.backend.dto.part.PartPatchDTO;
import com.emms.backend.dto.part.StockTransactionDTO;
import com.emms.backend.dto.part.UsePartDTO;
import com.emms.backend.entity.InventoryCount;
import com.emms.backend.entity.InventoryCountItem;
import com.emms.backend.entity.InventoryMonthlyClosing;
import com.emms.backend.entity.Part;
import com.emms.backend.entity.PartTransaction;
import com.emms.backend.service.PartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/parts")
@Tag(name = "Parts", description = "Operations on parts")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN','ROLE_OPERATOR')")
    public ResponseEntity<Collection<Part>> getAll() {
        return ResponseEntity.ok(partService.getAll());
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN')")
    public ResponseEntity<List<PartTransaction>> getAllTransactions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type
    ) {
        return ResponseEntity.ok(
                partService.getAllTransactions(keyword, type)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN','ROLE_OPERATOR')")
    public ResponseEntity<Part> getById(@PathVariable Long id) {
        return ResponseEntity.ok(partService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Part> create(@Valid @RequestBody Part partReq) {
        Part saved = partService.create(partReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Part> patch(
            @Valid @RequestBody PartPatchDTO part,
            @PathVariable Long id
    ) {
        Part updated = partService.update(id, part);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        partService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }

    @PutMapping("/{id}/import-stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Part> importStock(
            @PathVariable Long id,
            @RequestBody StockTransactionDTO dto
    ) {
        return ResponseEntity.ok(
                partService.importStock(
                        id,
                        dto.getQuantity(),
                        dto.getNote()
                )
        );
    }

    @PostMapping("/work-orders/{workOrderId}/use")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN')")
    public ResponseEntity<Part> usePartForWorkOrder(
            @PathVariable Long workOrderId,
            @RequestBody UsePartDTO dto
    ) {
        return ResponseEntity.ok(
                partService.usePartForWorkOrder(
                        workOrderId,
                        dto.getPartId(),
                        dto.getQuantity()
                )
        );
    }

    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN')")
    public ResponseEntity<List<PartTransaction>> getTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(partService.getTransactionsByPart(id));
    }

    @PostMapping("/inventory-counts")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<InventoryCount> createInventoryCount(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(
                partService.createInventoryCount(year, month, note)
        );
    }

    @PostMapping("/inventory-counts/{id}/items")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<InventoryCountItem> addInventoryCountItem(
            @PathVariable Long id,
            @RequestBody InventoryCountItemDTO dto
    ) {
        return ResponseEntity.ok(
                partService.addInventoryCountItem(id, dto)
        );
    }

    @PutMapping("/inventory-counts/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<InventoryCount> confirmInventoryCount(@PathVariable Long id) {
        return ResponseEntity.ok(
                partService.confirmInventoryCount(id)
        );
    }

    @DeleteMapping("/inventory-counts/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<SuccessResponse> deleteInventoryCount(@PathVariable Long id) {
        partService.deleteInventoryCount(id);

        return ResponseEntity.ok(
                new SuccessResponse(true, "Xóa phiếu kiểm kê thành công")
        );
    }

    @PostMapping("/monthly-closing")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<InventoryMonthlyClosing> closeMonth(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(
                partService.closeMonth(year, month, note)
        );
    }

    @PutMapping("/monthly-closing/{id}/reopen")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<InventoryMonthlyClosing> reopenMonthlyClosing(@PathVariable Long id) {
        return ResponseEntity.ok(
                partService.reopenMonthlyClosing(id)
        );
    }

    @GetMapping("/inventory-counts/{id}/items")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<List<InventoryCountItem>> getInventoryCountItems(@PathVariable Long id) {
    return ResponseEntity.ok(
            partService.getInventoryCountItems(id));
        }
}