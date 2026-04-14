package com.emms.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.TaskBase;

public interface TaskBaseRepository extends JpaRepository<TaskBase, Long> {
    
    
}
