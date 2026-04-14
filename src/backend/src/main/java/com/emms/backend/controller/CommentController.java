package com.emms.backend.controller;

import com.emms.backend.dto.comment.CommentCriteria;
import com.emms.backend.dto.comment.CommentPatchDTO;
import com.emms.backend.dto.comment.CommentPostDTO;
import com.emms.backend.entity.Comment;
import com.emms.backend.entity.User;
import com.emms.backend.service.CommentService;
import com.emms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Controller", description = "APIs quản lý comment")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tạo comment mới")
    public ResponseEntity<Comment> create(@Valid @RequestBody CommentPostDTO dto,
                                          HttpServletRequest request) {
        User currentUser = userService.whoami(request);
        Comment created = commentService.create(dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhật comment")
    public ResponseEntity<Comment> update(@PathVariable Long id,
                                          @Valid @RequestBody CommentPatchDTO dto,
                                          HttpServletRequest request) {
        User currentUser = userService.whoami(request);
        Comment updated = commentService.update(id, dto, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa comment")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy comment theo id")
    public ResponseEntity<Comment> getById(@PathVariable Long id) {
        Optional<Comment> comment = commentService.findById(id);
        return comment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    @Operation(summary = "Lấy tất cả comment")
    public ResponseEntity<List<Comment>> getAll() {
        return ResponseEntity.ok(commentService.getAll());
    }

    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tìm comment theo điều kiện")
    public ResponseEntity<List<Comment>> search(@RequestBody CommentCriteria criteria,
                                                HttpServletRequest request) {
        User currentUser = userService.whoami(request);
        return ResponseEntity.ok(commentService.findByCriteria(criteria, currentUser));
    }

    @GetMapping("/work-orders/{workOrderId}/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đếm số comment theo work order")
    public ResponseEntity<Long> countByWorkOrder(@PathVariable Long workOrderId,
                                                 HttpServletRequest request) {
        User currentUser = userService.whoami(request);
        long count = commentService.countByWorkOrderId(workOrderId, currentUser);
        return ResponseEntity.ok(count);
    }
}