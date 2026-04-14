package com.emms.backend.mapper;

import com.emms.backend.dto.comment.CommentPatchDTO;
import com.emms.backend.dto.comment.CommentPostDTO;
import com.emms.backend.dto.comment.CommentShowDTO;
import com.emms.backend.entity.Comment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        uses = {
                UserMapper.class
        }
)
public interface CommentMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "workOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Comment updateComment(@MappingTarget Comment entity, CommentPatchDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "workOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Comment fromPostDto(CommentPostDTO dto);

    @Mapping(target = "system", constant = "false")
    CommentShowDTO toShowDto(Comment model);
}