package com.emms.backend.service;

import com.emms.backend.dto.importData.AssetImportDTO;
import com.emms.backend.dto.importData.ImportResponse;
import com.emms.backend.dto.importData.LocationImportDTO;
import com.emms.backend.dto.importData.MeterImportDTO;
import com.emms.backend.dto.importData.PartImportDTO;
import com.emms.backend.dto.importData.PreventiveMaintenanceImportDTO;
import com.emms.backend.dto.importData.WorkOrderImportDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.Location;
import com.emms.backend.entity.Meter;
import com.emms.backend.entity.Part;
import com.emms.backend.entity.PreventiveMaintenance;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportService {

    private final AssetService assetService;
    private final LocationService locationService;
    private final PartService partService;
    private final MeterService meterService;
    private final WorkOrderService workOrderService;
    private final PreventiveMaintenanceService preventiveMaintenanceService;

    // ================== ASSET ==================
    public ImportResponse importAssets(List<AssetImportDTO> list) {
        if (list == null || list.isEmpty()) {
            return empty("No asset data to import");
        }

        validateAssetDtos(list);
        checkDuplicateAssetBarcodes(list);

        List<Asset> entities = new ArrayList<>();
        Map<String, Asset> assetsByName = new HashMap<>();

        AssetImportDTO[] ordered = assetService.orderAssets(list);
        for (AssetImportDTO dto : ordered) {
            Asset entity = new Asset();
            assetService.setAssetFieldsFromImportDto(entity, dto, assetsByName);
            entities.add(entity);

            if (entity.getName() != null && !entity.getName().isBlank()) {
                assetsByName.put(entity.getName().trim().toLowerCase(), entity);
            }
        }

        assetService.saveAll(entities);

        return ImportResponse.builder()
                .total(list.size())
                .created(entities.size())
                .updated(0)
                .failed(0)
                .message("Assets imported successfully")
                .build();
    }

    // ================== LOCATION ==================
    public ImportResponse importLocations(List<LocationImportDTO> list) {
        if (list == null || list.isEmpty()) {
            return empty("No location data to import");
        }

        validateLocationDtos(list);

        List<Location> entities = new ArrayList<>();
        Map<String, Location> locationsByName = new HashMap<>();

        for (LocationImportDTO dto : locationService.orderLocations(list)) {
            Location entity = new Location();
            locationService.setLocationFieldsFromImportDto(entity, dto, locationsByName);
            entities.add(entity);

            if (entity.getName() != null) {
                locationsByName.put(entity.getName(), entity);
            }
        }

        locationService.save(entities);

        return ImportResponse.builder()
                .total(list.size())
                .created(entities.size())
                .updated(0)
                .failed(0)
                .message("Locations imported successfully")
                .build();
    }

    // ================== METER ==================
    public ImportResponse importMeters(List<MeterImportDTO> list) {
        if (list == null || list.isEmpty()) {
            return empty("No meter data to import");
        }

        validateMeterDtos(list);

        List<Meter> entities = new ArrayList<>();

        for (MeterImportDTO dto : list) {
            Meter entity = new Meter();
            meterService.importMeter(entity, dto);
            entities.add(entity);
        }

        meterService.save(entities);

        return ImportResponse.builder()
                .total(list.size())
                .created(entities.size())
                .updated(0)
                .failed(0)
                .message("Meters imported successfully")
                .build();
    }

    // ================== PART ==================
    public ImportResponse importParts(List<PartImportDTO> list) {
        if (list == null || list.isEmpty()) {
            return empty("No part data to import");
        }

        validatePartDtos(list);
        checkDuplicatePartBarcodes(list);

        List<Part> entities = new ArrayList<>();

        for (PartImportDTO dto : list) {
            Part entity = new Part();
            partService.importPart(entity, dto);
            entities.add(entity);
        }

        partService.saveAll(entities);

        return ImportResponse.builder()
                .total(list.size())
                .created(entities.size())
                .updated(0)
                .failed(0)
                .message("Parts imported successfully")
                .build();
    }

    // ================== WORK ORDER ==================
    public ImportResponse importWorkOrders(List<WorkOrderImportDTO> list) {
        if (list == null || list.isEmpty()) {
            return empty("No work order data to import");
        }

        validateWorkOrderDtos(list);

        List<WorkOrder> entities = new ArrayList<>();

        for (WorkOrderImportDTO dto : list) {
            WorkOrder entity = new WorkOrder();
            workOrderService.importWorkOrder(entity, dto);
            entities.add(entity);
        }

        workOrderService.saveAll(entities);

        return ImportResponse.builder()
                .total(list.size())
                .created(entities.size())
                .updated(0)
                .failed(0)
                .message("Work orders imported successfully")
                .build();
    }

    // ================== PREVENTIVE MAINTENANCE ==================
    public ImportResponse importPreventiveMaintenances(List<PreventiveMaintenanceImportDTO> list) {
        if (list == null || list.isEmpty()) {
            return empty("No preventive maintenance data to import");
        }

        validatePreventiveMaintenanceDtos(list);

        List<PreventiveMaintenance> entities = new ArrayList<>();

        for (PreventiveMaintenanceImportDTO dto : list) {
            PreventiveMaintenance entity = new PreventiveMaintenance();
            preventiveMaintenanceService.importPreventiveMaintenance(entity, dto);
            entities.add(entity);
        }

        preventiveMaintenanceService.saveAll(entities);

        return ImportResponse.builder()
                .total(list.size())
                .created(entities.size())
                .updated(0)
                .failed(0)
                .message("Preventive maintenances imported successfully")
                .build();
    }

    // ================== COMMON ==================
    private ImportResponse empty(String message) {
        return ImportResponse.builder()
                .total(0)
                .created(0)
                .updated(0)
                .failed(0)
                .message(message)
                .build();
    }

    private void validateAssetDtos(List<AssetImportDTO> dtos) {
        for (int i = 0; i < dtos.size(); i++) {
            AssetImportDTO dto = dtos.get(i);
            if (dto == null) {
                throw new CustomException("Asset at index " + i + " must not be null", HttpStatus.BAD_REQUEST);
            }
            dto.validate();
        }
    }

    private void validateLocationDtos(List<LocationImportDTO> dtos) {
        for (int i = 0; i < dtos.size(); i++) {
            LocationImportDTO dto = dtos.get(i);
            if (dto == null) {
                throw new CustomException("Location at index " + i + " must not be null", HttpStatus.BAD_REQUEST);
            }
            dto.validate();
        }
    }

    private void validateMeterDtos(List<MeterImportDTO> dtos) {
        for (int i = 0; i < dtos.size(); i++) {
            MeterImportDTO dto = dtos.get(i);
            if (dto == null) {
                throw new CustomException("Meter at index " + i + " must not be null", HttpStatus.BAD_REQUEST);
            }
            dto.validate();
        }
    }

    private void validatePartDtos(List<PartImportDTO> dtos) {
        for (int i = 0; i < dtos.size(); i++) {
            PartImportDTO dto = dtos.get(i);
            if (dto == null) {
                throw new CustomException("Part at index " + i + " must not be null", HttpStatus.BAD_REQUEST);
            }
            dto.validate();
        }
    }

    private void validateWorkOrderDtos(List<WorkOrderImportDTO> dtos) {
        for (int i = 0; i < dtos.size(); i++) {
            WorkOrderImportDTO dto = dtos.get(i);
            if (dto == null) {
                throw new CustomException("Work order at index " + i + " must not be null", HttpStatus.BAD_REQUEST);
            }
            dto.validate();
        }
    }

    private void validatePreventiveMaintenanceDtos(List<PreventiveMaintenanceImportDTO> dtos) {
        for (int i = 0; i < dtos.size(); i++) {
            PreventiveMaintenanceImportDTO dto = dtos.get(i);
            if (dto == null) {
                throw new CustomException("Preventive maintenance at index " + i + " must not be null", HttpStatus.BAD_REQUEST);
            }
            dto.validate();
        }
    }

    private void checkDuplicateAssetBarcodes(List<AssetImportDTO> dtos) {
        HashSet<String> seen = new HashSet<>();

        for (AssetImportDTO dto : dtos) {
            String code = normalize(dto.getBarCode());
            if (code != null && !seen.add(code)) {
                throw new CustomException("Duplicate asset barcode in import file: " + code, HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void checkDuplicatePartBarcodes(List<PartImportDTO> dtos) {
        HashSet<String> seen = new HashSet<>();

        for (PartImportDTO dto : dtos) {
            String code = normalize(dto.getBarcode());
            if (code != null && !seen.add(code)) {
                throw new CustomException("Duplicate part barcode in import file: " + code, HttpStatus.BAD_REQUEST);
            }
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }
}