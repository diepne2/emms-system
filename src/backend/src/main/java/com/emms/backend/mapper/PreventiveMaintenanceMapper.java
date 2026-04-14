package com.emms.backend.mapper;

import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceShowDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceSummaryDTO;
import com.emms.backend.entity.PreventiveMaintenance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PreventiveMaintenanceMapper {

    PreventiveMaintenanceDTO toDto(PreventiveMaintenance model);

    @Mapping(source = "id", target = "id")
    PreventiveMaintenanceShowDTO toShowDto(PreventiveMaintenance model);

    @Mapping(source = "id", target = "id")
    PreventiveMaintenanceSummaryDTO toSummaryDto(PreventiveMaintenance model);
}