package com.emms.backend.mapper;

import com.emms.backend.dto.location.LocationDTO;
import com.emms.backend.dto.location.LocationShowDTO;
import com.emms.backend.dto.location.LocationSummaryDTO;
import com.emms.backend.entity.Location;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDTO toDto(Location entity);

    LocationShowDTO toShowDto(Location entity);

    LocationSummaryDTO toSummaryDto(Location entity);

    Location fromDto(LocationDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateLocationFromDto(LocationDTO dto, @MappingTarget Location entity);
}