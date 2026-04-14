package com.emms.backend.mapper;

import com.emms.backend.dto.meter.MeterDTO;
import com.emms.backend.dto.meter.MeterShowDTO;
import com.emms.backend.dto.meter.MeterSummaryDTO;
import com.emms.backend.entity.Meter;
import com.emms.backend.entity.User;
import com.emms.backend.service.ReadingService;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MeterMapper {

    @Mapping(target = "assetId", source = "asset.id")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "meterCategoryId", ignore = true)
    @Mapping(target = "imageId", source = "image.assetId")
    @Mapping(target = "userIds", expression = "java(mapUserIds(entity.getUsers()))")
    MeterDTO toDto(Meter entity);

    @Mapping(target = "lastReading", ignore = true)
    @Mapping(target = "nextReading", ignore = true)
    MeterShowDTO toShowDto(Meter entity);

    default MeterShowDTO toShowDto(Meter entity, ReadingService readingService) {
        if (entity == null) {
            return null;
        }

        MeterShowDTO dto = toShowDto(entity);

        if (readingService != null && entity.getId() != null) {
            try {
                readingService.findLatestByMeter(entity.getId())
                        .ifPresent(reading -> {
                            LocalDateTime recordedAt = reading.getRecordedAt();
                            dto.setLastReading(recordedAt);

                            if (recordedAt != null && entity.getUpdateFrequency() != null) {
                                dto.setNextReading(recordedAt.plusMinutes(entity.getUpdateFrequency()));
                            }
                        });
            } catch (Exception ignored) {
            }
        }

        return dto;
    }

    MeterSummaryDTO toSummaryDto(Meter entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "meterCategory", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "demo", ignore = true)
    Meter fromDto(MeterDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "meterCategory", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "demo", ignore = true)
    void updateMeter(@MappingTarget Meter entity, MeterDTO dto);

    default Collection<Long> mapUserIds(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> ids = new ArrayList<>();
        for (User user : users) {
            if (user != null && user.getUserId() != null) {
                ids.add(user.getUserId());
            }
        }
        return ids;
    }
}