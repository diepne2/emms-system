package com.emms.backend.service;

import com.emms.backend.dto.task.TaskBaseDTO;
import com.emms.backend.entity.ChecklistTask;
import com.emms.backend.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ChecklistTaskService {

    public ChecklistTask createFromTaskBaseDTO(TaskBaseDTO dto) {
        if (dto == null) {
            throw new CustomException("Task data must not be null", HttpStatus.BAD_REQUEST);
        }

        if (dto.getLabel() == null || dto.getLabel().isBlank()) {
            throw new CustomException("Task label must not be blank", HttpStatus.BAD_REQUEST);
        }

        ChecklistTask task = new ChecklistTask();
        task.setTitle(dto.getLabel());              
        task.setDescription(dto.getDescription());  
        task.setRequired(false);                    
        task.setDisplayOrder(0);                   

        return task;
    }
}