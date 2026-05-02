package com.emms.backend.mapper;

import com.emms.backend.dto.category.CategorySummaryDTO;
import com.emms.backend.dto.user.UserSummaryDTO;
import com.emms.backend.dto.workorder.WorkOrderDTO;
import com.emms.backend.dto.workorder.WorkOrderPostDTO;
import com.emms.backend.dto.workorder.WorkOrderShowDTO;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrder;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface WorkOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "completedOn", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "assetName", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "contractors", ignore = true)
    WorkOrder fromPostDto(WorkOrderPostDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "completedOn", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "assetName", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "contractors", ignore = true)
    WorkOrder fromDto(WorkOrderDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "completedOn", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "assetName", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "contractors", ignore = true)
    void updateWorkOrder(@MappingTarget WorkOrder entity, WorkOrderDTO dto);

    @Mapping(target = "assignedTo", expression = "java(mapUser(entity.getAssignedTo()))")
    @Mapping(target = "category", expression = "java(mapCategory(entity.getCategory()))")
    @Mapping(target = "primaryUser", expression = "java(mapUser(entity.getPrimaryUser()))")
    @Mapping(target = "assetName", expression = "java(entity.getAsset() != null ? entity.getAsset().getName() : entity.getAssetName())")
    @Mapping(target = "assignedToId", expression = "java(entity.getAssignedTo() != null ? entity.getAssignedTo().getUserId() : null)")
    @Mapping(target = "assetId", expression = "java(entity.getAsset() != null ? entity.getAsset().getId() : null)")
    @Mapping(target = "requiredSignature", source = "requiresSignature")
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "estimatedStartDate", ignore = true)
    WorkOrderShowDTO toShowDto(WorkOrder entity);

    default CategorySummaryDTO mapCategory(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        CategorySummaryDTO dto = new CategorySummaryDTO();
        dto.setName(value.trim());
        return dto;
    }

    default UserSummaryDTO mapUser(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setFullName(value.trim());
        return dto;
    }

    default UserSummaryDTO mapUser(User user) {
        if (user == null) {
            return null;
        }

        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());

        String fullName = (
                (user.getFirstName() != null ? user.getFirstName().trim() : "") + " " +
                (user.getLastName() != null ? user.getLastName().trim() : "")
        ).trim();

        if (fullName.isEmpty()) {
            fullName = user.getFullName();
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = user.getUsername();
        }

        dto.setFullName(fullName);
        return dto;
    }

    default Long map(User user) {
        return user == null ? null : user.getUserId();
    }
}