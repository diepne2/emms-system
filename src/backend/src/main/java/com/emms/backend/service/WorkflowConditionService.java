package com.emms.backend.service;
import com.emms.backend.dto.woflow.*;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.WorkflowConditionMapper;
import com.emms.backend.entity.WorkflowCondition;
import com.emms.backend.repository.WorkflowConditionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowConditionService {

    private final WorkflowConditionRepository workflowConditionRepository;
    private final WorkflowConditionMapper workflowConditionMapper;

    public WorkflowCondition create(WorkflowCondition workflowCondition) {
        if (workflowCondition == null) {
            throw new CustomException("Workflow condition must not be null", HttpStatus.BAD_REQUEST);
        }
        return workflowConditionRepository.save(workflowCondition);
    }

    public WorkflowCondition create(WorkflowConditionPostDTO dto) {
        if (dto == null) {
            throw new CustomException("Workflow condition data must not be null", HttpStatus.BAD_REQUEST);
        }

        WorkflowCondition workflowCondition = workflowConditionMapper.toModel(dto);
        return workflowConditionRepository.save(workflowCondition);
    }

    public WorkflowCondition update(Long id, WorkflowConditionDTO workflowConditionDTO) {
        if (workflowConditionDTO == null) {
            throw new CustomException("Workflow condition patch data must not be null", HttpStatus.BAD_REQUEST);
        }

        WorkflowCondition savedWorkflowCondition = workflowConditionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Workflow condition not found", HttpStatus.NOT_FOUND));

        workflowConditionMapper.updateWorkflowCondition(savedWorkflowCondition, workflowConditionDTO);
        return workflowConditionRepository.save(savedWorkflowCondition);
    }

    @Transactional(readOnly = true)
    public Collection<WorkflowCondition> getAll() {
        return workflowConditionRepository.findAll();
    }

    public void delete(Long id) {
        WorkflowCondition workflowCondition = workflowConditionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Workflow condition not found", HttpStatus.NOT_FOUND));

        workflowConditionRepository.delete(workflowCondition);
    }

    @Transactional(readOnly = true)
    public WorkflowCondition findById(Long id) {
        return workflowConditionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Workflow condition not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Collection<WorkflowCondition> findByWorkflowId(Long workflowId) {
        return workflowConditionRepository.findByWorkflow_Id(workflowId);
    }

    public Collection<WorkflowCondition> saveAll(Collection<WorkflowCondition> workflowConditions) {
        if (workflowConditions == null) {
            throw new CustomException("Workflow conditions must not be null", HttpStatus.BAD_REQUEST);
        }
        return workflowConditionRepository.saveAll(workflowConditions);
    }
}