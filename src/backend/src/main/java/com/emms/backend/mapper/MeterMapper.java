package com.emms.backend.mapper;

import com.emms.backend.dto.meter.MeterDTO;
import com.emms.backend.dto.meter.MeterShowDTO;
import com.emms.backend.dto.meter.MeterSummaryDTO;
import com.emms.backend.entity.Meter;
import com.emms.backend.entity.Reading;
import com.emms.backend.service.ReadingService;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper(
        componentModel = "spring",
        uses = {
                LocationMapper.class,
                AssetMapper.class,
                UserMapper.class,
                FileMapper.class,
                MeterCategoryMapper.class
        }
)
public interface MeterMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "meterCategory", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "demo", ignore = true)
    Meter updateMeter(@MappingTarget Meter entity, MeterDTO dto);

    @Mapping(source = "meterCategory.id", target = "meterCategoryId")
    @Mapping(source = "image.id", target = "imageId")
    @Mapping(source = "location.locationId", target = "locationId")
    @Mapping(source = "asset.assetId", target = "assetId")
    @Mapping(
            target = "userIds",
            expression = "java(model.getUsers() == null ? new ArrayList<>() : model.getUsers().stream().map(com.emms.backend.entity.User::getUserId).collect(Collectors.toList()))"
    )
    MeterDTO toPatchDto(Meter model);

    @Mapping(source = "id", target = "id")
    MeterShowDTO toShowDto(Meter model, @Context ReadingService readingService);

    @AfterMapping
    default void enrichShowDto(
            Meter model,
            @MappingTarget MeterShowDTO target,
            @Context ReadingService readingService
    ) {
        if (model == null || model.getId() == null || target == null) {
            return;
        }

        Optional<Reading> latestOptional = readingService.findLatestByMeter(model.getId());
        if (latestOptional.isEmpty()) {
            return;
        }

        Reading latest = latestOptional.get();
        LocalDateTime lastReading = latest.getRecordedAt();
        target.setLastReading(lastReading);

        Integer updateFrequency = model.getUpdateFrequency();
        if (lastReading != null && updateFrequency != null && updateFrequency > 0) {
            target.setNextReading(lastReading.plusDays(updateFrequency));
        }
    }

    @Mapping(source = "id", target = "id")
    MeterSummaryDTO toSummaryDto(Meter model);
}