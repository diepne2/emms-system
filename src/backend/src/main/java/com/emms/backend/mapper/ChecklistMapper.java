package com.emms.backend.mapper;

import com.emms.backend.dto.checklist.ChecklistSummaryDTO;
import com.emms.backend.entity.Checklist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChecklistMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ChecklistSummaryDTO toSummaryDTO(Checklist checklist);
}