package com.emms.backend.mapper;

import com.emms.backend.dto.request.RequestDTO;
import com.emms.backend.dto.request.RequestShowDTO;
import com.emms.backend.dto.request.RequestSummaryDTO;
import com.emms.backend.entity.Request;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "requestPortal", ignore = true)
    @Mapping(target = "workOrder", ignore = true)
    Request updateRequest(@MappingTarget Request entity, RequestDTO dto);

    @Mapping(target = "requestId", source = "id")
    @Mapping(target = "locationId", source = "location.locationId")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "requestPortalId", source = "requestPortal.id")
    @Mapping(target = "requestPortalTitle", source = "requestPortal.title")
    @Mapping(target = "workOrderId", source = "workOrder.id")
    @Mapping(target = "workOrderTitle", source = "workOrder.title")
    RequestDTO toDto(Request entity);

    @Mapping(target = "requestId", source = "id")
    @Mapping(target = "locationId", source = "location.locationId")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "requestPortalId", source = "requestPortal.id")
    @Mapping(target = "requestPortalTitle", source = "requestPortal.title")
    @Mapping(target = "workOrderId", source = "workOrder.id")
    @Mapping(target = "workOrderTitle", source = "workOrder.title")
    RequestShowDTO toShowDto(Request entity);

    @Mapping(target = "requestId", source = "id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "requestPortalTitle", source = "requestPortal.title")
    @Mapping(target = "workOrderTitle", source = "workOrder.title")
    RequestSummaryDTO toSummaryDto(Request entity);
}