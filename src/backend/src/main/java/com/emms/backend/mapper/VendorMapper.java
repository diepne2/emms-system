package com.emms.backend.mapper;


import com.emms.backend.dto.vendor.*;
import com.emms.backend.entity.Vendor;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface VendorMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Vendor updateVendor(@MappingTarget Vendor entity, VendorPatchDTO dto);

    VendorPatchDTO toPatchDto(Vendor model);

    VendorSummaryDTO toSummaryDto(Vendor model);
}