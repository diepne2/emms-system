package com.emms.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.emms.backend.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> , JpaSpecificationExecutor<Comment>{
    long countByWorkOrderId(Long workOrderId);

    List<Comment> findByUser_UserId(Long userId);

    
}
