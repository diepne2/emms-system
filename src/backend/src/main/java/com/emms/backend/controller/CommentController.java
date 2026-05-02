package com.emms.backend.controller;

import com.emms.backend.dto.comment.CommentCriteria;
import com.emms.backend.dto.comment.CommentPatchDTO;
import com.emms.backend.dto.comment.CommentPostDTO;
import com.emms.backend.dto.comment.CommentShowDTO;
import com.emms.backend.entity.Comment;
import com.emms.backend.entity.User;
import com.emms.backend.mapper.CommentMapper;
import com.emms.backend.service.CommentService;
import com.emms.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<CommentShowDTO>> findByCriteria(@RequestParam Long workOrderId,
                                                               Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        CommentCriteria criteria = new CommentCriteria();
        criteria.setWorkOrderId(workOrderId);

        List<Comment> comments = commentService.findByCriteria(criteria, currentUser);
        return ResponseEntity.ok(commentMapper.toShowDtoList(comments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentShowDTO> findById(@PathVariable Long id,
                                                   Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Comment comment = commentService.findEntityById(id);

        if (comment.getWorkOrder() != null && comment.getWorkOrder().getId() != null) {
            commentService.findByCriteria(buildCriteria(comment.getWorkOrder().getId()), currentUser);
        }

        return ResponseEntity.ok(commentMapper.toShowDto(comment));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countByWorkOrderId(@RequestParam Long workOrderId,
                                                                Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        long count = commentService.countByWorkOrderId(workOrderId, currentUser);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping
    public ResponseEntity<CommentShowDTO> create(@Valid @RequestBody CommentPostDTO dto,
                                                 Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Comment created = commentService.create(dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentMapper.toShowDto(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommentShowDTO> update(@PathVariable Long id,
                                                 @RequestBody CommentPatchDTO dto,
                                                 Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Comment updated = commentService.update(id, dto, currentUser);
        return ResponseEntity.ok(commentMapper.toShowDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Comment comment = commentService.findEntityById(id);
        if (comment.getUser() == null || !comment.getUser().getUserId().equals(currentUser.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private CommentCriteria buildCriteria(Long workOrderId) {
        CommentCriteria criteria = new CommentCriteria();
        criteria.setWorkOrderId(workOrderId);
        return criteria;
    }

    private User getCurrentUser(Authentication authentication) {
        String usernameOrEmail = authentication.getName();
        return userService.getByUsernameOrEmail(usernameOrEmail);
    }
}