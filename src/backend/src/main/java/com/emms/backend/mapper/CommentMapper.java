package com.emms.backend.mapper;

import com.emms.backend.dto.comment.CommentPatchDTO;
import com.emms.backend.dto.comment.CommentPostDTO;
import com.emms.backend.dto.comment.CommentShowDTO;
import com.emms.backend.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public Comment fromPostDto(CommentPostDTO dto) {
        if (dto == null) {
            return null;
        }

        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        return comment;
    }

    public void updateComment(Comment entity, CommentPatchDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        if (dto.getContent() != null) {
            entity.setContent(dto.getContent());
        }
    }

    public CommentShowDTO toShowDto(Comment entity) {
        if (entity == null) {
            return null;
        }

        CommentShowDTO dto = new CommentShowDTO();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
            dto.setWorkOrderTitle(entity.getWorkOrder().getTitle());
        }

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getUserId());
            dto.setUserFullName(entity.getUser().getFullName());
            dto.setUsername(entity.getUser().getUsername());
        }

        return dto;
    }

    public List<CommentShowDTO> toShowDtoList(List<Comment> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toShowDto)
                .collect(Collectors.toList());
    }
}