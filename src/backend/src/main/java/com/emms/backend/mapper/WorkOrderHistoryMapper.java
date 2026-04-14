package com.emms.backend.mapper;

import com.emms.backend.dto.wo_history.WorkOrderHistoryShowDTO;
import com.emms.backend.entity.WorkOrderHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class}
)
public interface WorkOrderHistoryMapper {

    @Mapping(target = "workOrderId", source = "workOrder.workOrderId")
    WorkOrderHistoryShowDTO toShowDto(WorkOrderHistory model);
}