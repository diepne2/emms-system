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
            throw new CustomException("Dữ liệu task không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (dto.getLabel() == null || dto.getLabel().isBlank()) {
            throw new CustomException("Label task không được để trống", HttpStatus.BAD_REQUEST);
        }

        ChecklistTask task = new ChecklistTask();
        task.setTitle(dto.getLabel());              
        task.setDescription(dto.getDescription());  
        task.setRequired(false);                    
        task.setDisplayOrder(0);                   

        return task;
    }
}