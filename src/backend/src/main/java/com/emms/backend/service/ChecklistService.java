package com.emms.backend.service;

import com.emms.backend.dto.checklist.ChecklistDTO;
import com.emms.backend.dto.task.TaskBaseDTO;
import com.emms.backend.entity.Checklist;
import com.emms.backend.entity.ChecklistTask;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.CheckListRepository;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ChecklistService {

    private final CheckListRepository checklistRepository;
    private final EntityManager em;

    public ChecklistService(
            CheckListRepository checklistRepository,
            EntityManager em
    ) {
        this.checklistRepository = checklistRepository;
        this.em = em;
    }

    public Checklist create(ChecklistDTO dto) {
        if (dto == null) {
            throw new CustomException("ChecklistDTO không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new CustomException("Tên checklist không được để trống", HttpStatus.BAD_REQUEST);
        }

        Checklist checklist = new Checklist();
        checklist.setName(dto.getName());
        checklist.setDescription(dto.getDescription());
        checklist.setAppliesTo(dto.getAppliesTo());
        checklist.setActive(dto.getActive() == null || dto.getActive());

        List<ChecklistTask> tasks = new ArrayList<>();
        if (dto.getTasks() != null) {
            int order = 1;
            for (TaskBaseDTO taskDto : dto.getTasks()) {
                ChecklistTask task = new ChecklistTask();
                task.setTitle(requireLabel(taskDto));
                task.setDescription(trim(taskDto.getDescription()));
                task.setTaskType(mapTaskType(taskDto));
                task.setRequired(true);
                task.setDisplayOrder(order++);
                task.setChecklist(checklist);
                tasks.add(task);
            }
        }

        checklist.setTasks(tasks);

        Checklist saved = checklistRepository.saveAndFlush(checklist);
        em.refresh(saved);
        return saved;
    }

    public Checklist update(Long id, ChecklistDTO dto) {
        Checklist existing = findEntityById(id);

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getAppliesTo() != null) {
            existing.setAppliesTo(dto.getAppliesTo());
        }
        if (dto.getActive() != null) {
            existing.setActive(dto.getActive());
        }

        if (dto.getTasks() != null) {
            existing.getTasks().clear();
            int order = 1;
            for (TaskBaseDTO taskDto : dto.getTasks()) {
                ChecklistTask task = new ChecklistTask();
                task.setTitle(requireLabel(taskDto));
                task.setDescription(trim(taskDto.getDescription()));
                task.setTaskType(mapTaskType(taskDto));
                task.setRequired(true);
                task.setDisplayOrder(order++);
                task.setChecklist(existing);
                existing.getTasks().add(task);
            }
        }

        Checklist saved = checklistRepository.saveAndFlush(existing);
        em.refresh(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Checklist findEntityById(Long id) {
        return checklistRepository.findById(id)
                .orElseThrow(() -> new CustomException("Checklist không tồn tại", HttpStatus.NOT_FOUND));
    }

    public void delete(Long id) {
        checklistRepository.delete(findEntityById(id));
    }

    private String requireLabel(TaskBaseDTO dto) {
        if (dto == null || dto.getLabel() == null || dto.getLabel().isBlank()) {
            throw new CustomException("Task title không được để trống", HttpStatus.BAD_REQUEST);
        }
        return dto.getLabel().trim();
    }

    private ChecklistTask.ChecklistTaskType mapTaskType(TaskBaseDTO dto) {
        if (dto == null || dto.getTaskType() == null) {
            return ChecklistTask.ChecklistTaskType.PASS_FAIL;
        }

        return switch (dto.getTaskType()) {
            case CHECK, PASS_FAIL -> ChecklistTask.ChecklistTaskType.PASS_FAIL;
            case NUMBER -> ChecklistTask.ChecklistTaskType.NUMBER;
            case TEXT, SUBTASK -> ChecklistTask.ChecklistTaskType.TEXT;
        };
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}