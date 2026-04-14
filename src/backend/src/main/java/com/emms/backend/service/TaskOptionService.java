package com.emms.backend.service;

import com.emms.backend.dto.task.TaskOptionPatchDTO;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.TaskOptionMapper;
import com.emms.backend.entity.TaskOption;
import com.emms.backend.repository.TaskOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskOptionService {

    private final TaskOptionRepository taskOptionRepository;
    private final TaskOptionMapper taskOptionMapper;

    public TaskOption create(TaskOption taskOption) {
        validateCreatePayload(taskOption);
        return taskOptionRepository.save(taskOption);
    }

    public TaskOption update(Long id, TaskOptionPatchDTO dto) {
        validatePatchPayload(id, dto);

        TaskOption savedTaskOption = taskOptionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Task option not found", HttpStatus.NOT_FOUND));

        taskOptionMapper.updateTaskOption(savedTaskOption, dto);
        return taskOptionRepository.save(savedTaskOption);
    }

    @Transactional(readOnly = true)
    public Collection<TaskOption> getAll() {
        return taskOptionRepository.findAll();
    }

    public void delete(Long id) {
        TaskOption existing = taskOptionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Task option not found", HttpStatus.NOT_FOUND));

        taskOptionRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public Optional<TaskOption> findById(Long id) {
        return taskOptionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public TaskOption findEntityById(Long id) {
        return taskOptionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Task option not found", HttpStatus.NOT_FOUND));
    }

    private void validateCreatePayload(TaskOption taskOption) {
        if (taskOption == null) {
            throw new CustomException("Task option payload must not be null", HttpStatus.BAD_REQUEST);
        }
    }

    private void validatePatchPayload(Long pathId, TaskOptionPatchDTO dto) {
        if (dto == null) {
            throw new CustomException("Task option payload must not be null", HttpStatus.BAD_REQUEST);
        }

        if (dto.getId() != null && !dto.getId().equals(pathId)) {
            throw new CustomException("Task option ID mismatch", HttpStatus.BAD_REQUEST);
        }
    }
}