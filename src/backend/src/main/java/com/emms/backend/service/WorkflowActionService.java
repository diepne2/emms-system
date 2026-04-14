package com.emms.backend.service;

import com.emms.backend.dto.woflow.*;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.WorkflowActionMapper;
import com.emms.backend.entity.WorkflowAction;
import com.emms.backend.repository.WorkflowActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowActionService {

    private final WorkflowActionRepository workflowActionRepository;
    private final WorkflowActionMapper workflowActionMapper;

    public WorkflowAction create(WorkflowActionPostDTO dto) {
        if (dto == null) {
            throw new CustomException("Workflow action data must not be null", HttpStatus.BAD_REQUEST);
        }

        WorkflowAction workflowAction = workflowActionMapper.toModel(dto);
        return workflowActionRepository.save(workflowAction);
    }

    public WorkflowAction create(WorkflowAction workflowAction) {
        if (workflowAction == null) {
            throw new CustomException("Workflow action must not be null", HttpStatus.BAD_REQUEST);
        }
        return workflowActionRepository.save(workflowAction);
    }

    public WorkflowAction update(Long id, WorkflowActionDTO workflowActionDTO) {
        if (workflowActionDTO == null) {
            throw new CustomException("Workflow action patch data must not be null", HttpStatus.BAD_REQUEST);
        }

        WorkflowAction savedWorkflowAction = workflowActionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Workflow action not found", HttpStatus.NOT_FOUND));

        workflowActionMapper.updateWorkflowAction(savedWorkflowAction, workflowActionDTO);
        return workflowActionRepository.save(savedWorkflowAction);
    }

    @Transactional(readOnly = true)
    public Collection<WorkflowAction> getAll() {
        return workflowActionRepository.findAll();
    }

    public void delete(Long id) {
        WorkflowAction workflowAction = workflowActionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Workflow action not found", HttpStatus.NOT_FOUND));

        workflowActionRepository.delete(workflowAction);
    }

    @Transactional(readOnly = true)
    public WorkflowAction findById(Long id) {
        return workflowActionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Workflow action not found", HttpStatus.NOT_FOUND));
    }
}