package com.emms.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.TaskOption;

public interface TaskOptionRepository extends JpaRepository<TaskOption, Long> {
    
}
