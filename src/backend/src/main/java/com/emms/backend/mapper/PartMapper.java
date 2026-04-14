package com.emms.backend.mapper;

import com.emms.backend.dto.part.PartPatchDTO;
import com.emms.backend.dto.part.PartShowDTO;
import com.emms.backend.dto.part.PartSummaryDTO;
import com.emms.backend.entity.Part;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PartMapper {

    PartSummaryDTO toSummaryDto(Part entity);

    PartShowDTO toShowDto(Part entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePartFromDto(PartPatchDTO dto, @MappingTarget Part entity);
}