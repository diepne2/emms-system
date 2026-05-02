package com.emms.backend.mapper;

import com.emms.backend.dto.meter.MeterDTO;
import com.emms.backend.dto.meter.MeterShowDTO;
import com.emms.backend.dto.meter.MeterSummaryDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.AssetDocument;
import com.emms.backend.entity.Location;
import com.emms.backend.entity.Meter;
import com.emms.backend.entity.MeterCategory;
import com.emms.backend.entity.User;
import com.emms.backend.service.ReadingService;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
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
    @Mapping(target = "imageId", ignore = true)
    @Mapping(target = "userIds", expression = "java(mapUserIds(entity.getUsers()))")
    MeterDTO toDto(Meter entity);

    @Mapping(target = "lastReading", ignore = true)
    @Mapping(target = "nextReading", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
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
    @Mapping(target = "asset", source = "assetId", qualifiedByName = "mapAsset")
    @Mapping(target = "location", source = "locationId", qualifiedByName = "mapLocation")
    @Mapping(target = "meterCategory", source = "meterCategoryId", qualifiedByName = "mapMeterCategory")
    @Mapping(target = "image", source = "imageId", qualifiedByName = "mapAssetDocument")
    @Mapping(target = "users", source = "userIds", qualifiedByName = "mapUsers")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "demo", ignore = true)
    Meter fromDto(MeterDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", source = "assetId", qualifiedByName = "mapAsset")
    @Mapping(target = "location", source = "locationId", qualifiedByName = "mapLocation")
    @Mapping(target = "meterCategory", source = "meterCategoryId", qualifiedByName = "mapMeterCategory")
    @Mapping(target = "image", source = "imageId", qualifiedByName = "mapAssetDocument")
    @Mapping(target = "users", source = "userIds", qualifiedByName = "mapUsers")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "demo", ignore = true)
    void updateEntityFromDto(MeterDTO dto, @MappingTarget Meter entity);


    @Named("mapAsset")
    default Asset mapAsset(Long id) {
        if (id == null) {
            return null;
        }
        Asset asset = new Asset();
        asset.setId(id);
        return asset;
    }

    @Named("mapLocation")
    default Location mapLocation(Long id) {
        if (id == null) {
            return null;
        }
        Location location = new Location();
        location.setId(id);
        return location;
    }

    @Named("mapMeterCategory")
    default MeterCategory mapMeterCategory(Long id) {
        if (id == null) {
            return null;
        }
        MeterCategory category = new MeterCategory();
        category.setAssetId(id);
        return category;
    }

    @Named("mapAssetDocument")
    default AssetDocument mapAssetDocument(Long id) {
        if (id == null) {
            return null;
        }
        AssetDocument document = new AssetDocument();
        document.setAssetId(id);
        return document;
    }

    @Named("mapUsers")
    default List<User> mapUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> users = new ArrayList<>();
        for (Long userId : userIds) {
            if (userId != null) {
                User user = new User();
                user.setUserId(userId);
                users.add(user);
            }
        }
        return users;
    }

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