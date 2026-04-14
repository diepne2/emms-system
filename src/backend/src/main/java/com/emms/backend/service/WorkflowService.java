package com.emms.backend.service;

import com.emms.backend.dto.woflow.*;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.WorkflowActionMapper;
import com.emms.backend.mapper.WorkflowConditionMapper;
import com.emms.backend.mapper.WorkflowMapper;
import com.emms.backend.entity.PreventiveMaintenance;
import com.emms.backend.entity.Schedule;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.Workflow;
import com.emms.backend.entity.WorkflowAction;
import com.emms.backend.entity.WorkflowCondition;
import com.emms.backend.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;
    private final WorkflowConditionMapper workflowConditionMapper;
    private final WorkflowActionMapper workflowActionMapper;

    public Workflow create(Workflow workflow) {
        if (workflow == null) {
            throw new CustomException("Workflow must not be null", HttpStatus.BAD_REQUEST);
        }

        syncRelations(workflow);
        return workflowRepository.save(workflow);
    }

    public Workflow create(WorkflowPostDTO dto) {
        if (dto == null) {
            throw new CustomException("Workflow data must not be null", HttpStatus.BAD_REQUEST);
        }

        Workflow workflow = workflowMapper.toModel(dto);

        // map secondary conditions
        workflow.setSecondaryConditions(Collections.emptyList());
        if (dto.getSecondaryConditions() != null) {
            for (var item : dto.getSecondaryConditions()) {
                WorkflowCondition condition = workflowConditionMapper.toModel(item);
                workflow.addCondition(condition);
            }
        }

        // map action
        if (dto.getAction() != null) {
            WorkflowAction action = workflowActionMapper.toModel(dto.getAction());
            workflow.setAction(action);
        }

        syncRelations(workflow);
        return workflowRepository.save(workflow);
    }

    public Workflow update(Long id, WorkflowDTO workflowDTO) {
        if (workflowDTO == null) {
            throw new CustomException("Workflow patch data must not be null", HttpStatus.BAD_REQUEST);
        }

        Workflow savedWorkflow = workflowRepository.findById(id)
                .orElseThrow(() -> new CustomException("Workflow not found", HttpStatus.NOT_FOUND));

        workflowMapper.updateWorkflow(savedWorkflow, workflowDTO);

        syncRelations(savedWorkflow);
        return workflowRepository.save(savedWorkflow);
    }

    @Transactional(readOnly = true)
    public Collection<Workflow> getAll() {
        return workflowRepository.findAll();
    }

    public void delete(Long id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new CustomException("Workflow not found", HttpStatus.NOT_FOUND));

        workflowRepository.delete(workflow);
    }

    @Transactional(readOnly = true)
    public Workflow findById(Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new CustomException("Workflow not found", HttpStatus.NOT_FOUND));
    }

    private void syncRelations(Workflow workflow) {
        if (workflow == null) {
            return;
        }

        List<WorkflowCondition> conditions = workflow.getSecondaryConditions();
        if (conditions != null) {
            for (WorkflowCondition condition : conditions) {
                if (condition != null) {
                    condition.setWorkflow(workflow);
                }
            }
        }

        WorkflowAction action = workflow.getAction();
        if (action != null) {
            action.setWorkflow(workflow);
        }
    }

    
}