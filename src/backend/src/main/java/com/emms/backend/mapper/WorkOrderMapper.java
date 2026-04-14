package com.emms.backend.mapper;

import com.emms.backend.dto.workorder.WorkOrderDTO;
import com.emms.backend.dto.workorder.WorkOrderPostDTO;
import com.emms.backend.dto.workorder.WorkOrderShowDTO;
import com.emms.backend.dto.workorder.WorkOrderSummaryDTO;
import com.emms.backend.dto.workorderBase.WorkOrderBaseSummaryDTO;
import com.emms.backend.entity.WorkOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WorkOrderMapper {

    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateWorkOrder(@MappingTarget WorkOrder entity, WorkOrderDTO dto);

    @Mapping(target = "assignedToId", source = "assignedTo.userId")
    WorkOrderDTO toPatchDto(WorkOrder model);

    WorkOrderShowDTO toShowDto(WorkOrder model);

    WorkOrderSummaryDTO toSummaryDto(WorkOrder model);

    WorkOrderBaseSummaryDTO toBaseMiniDto(WorkOrder model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "assetName", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "completedOn", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    WorkOrder fromPostDto(WorkOrderPostDTO dto);
}