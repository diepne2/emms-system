package com.emms.backend.mapper;

import com.emms.backend.dto.workorder.WorkOrderDTO;
import com.emms.backend.dto.workorder.WorkOrderPostDTO;
import com.emms.backend.dto.workorder.WorkOrderShowDTO;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrder;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "primaryUser", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "contractors", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "completedOn", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "assetName", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    WorkOrder fromPostDto(WorkOrderPostDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "primaryUser", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "contractors", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "completedOn", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "assetName", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    WorkOrder fromDto(WorkOrderDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "primaryUser", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "contractors", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "completedOn", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "assetName", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateWorkOrder(@MappingTarget WorkOrder entity, WorkOrderDTO dto);

    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "primaryUser", ignore = true)
    WorkOrderShowDTO toShowDto(WorkOrder entity);

    default Long map(User user) {
        return user == null ? null : user.getUserId();
    }

    default List<com.emms.backend.dto.user.UserSummaryDTO> mapUserToList(User user) {
        return java.util.Collections.emptyList();
    }
}