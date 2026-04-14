package com.emms.backend.mapper;

import com.emms.backend.dto.wo_history.WorkOrderHistoryShowDTO;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrderHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkOrderHistoryMapper {

    @Mapping(target = "workOrderId", source = "workOrder.id")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    WorkOrderHistoryShowDTO toShowDto(WorkOrderHistory model);

    default String map(User user) {
        return user == null ? null : user.getUsername();
    }
}