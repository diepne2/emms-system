package com.emms.backend.service;

import com.emms.backend.dto.checklist.ChecklistDTO;
import com.emms.backend.dto.checklist.ChecklistPostDTO;
import com.emms.backend.dto.task.TaskBaseDTO;
import com.emms.backend.entity.Checklist;
import com.emms.backend.entity.ChecklistTask;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.CheckListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChecklistService {

    private final CheckListRepository checklistRepository;

    public Checklist createPost(ChecklistPostDTO request) {
        validateCreateRequest(request);

        Checklist checklist = new Checklist();
        checklist.setName(request.getName());
        checklist.setDescription(request.getDescription());
        checklist.setAppliesTo(defaultAppliesTo(request.getAppliesTo()));
        checklist.setActive(request.getActive() == null || request.getActive());

        checklist.setTasks(buildChecklistTasks(request.getTasks()));

        return checklistRepository.save(checklist);
    }

    public Checklist update(Long id, ChecklistDTO request) {
        Checklist checklist = checklistRepository.findById(id)
                .orElseThrow(() -> new CustomException("Checklist không tồn tại", HttpStatus.NOT_FOUND));

        validateUpdateRequest(request);

        if (request.getName() != null) {
            checklist.setName(request.getName());
        }

        checklist.setDescription(request.getDescription());

        if (request.getAppliesTo() != null) {
            checklist.setAppliesTo(defaultAppliesTo(request.getAppliesTo()));
        }

        if (request.getActive() != null) {
            checklist.setActive(request.getActive());
        }

        checklist.setTasks(buildChecklistTasks(request.getTasks()));

        return checklistRepository.save(checklist);
    }

    @Transactional(readOnly = true)
    public Collection<Checklist> getAll() {
        return checklistRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Checklist> findById(Long id) {
        return checklistRepository.findById(id);
    }

    public void delete(Long id) {
        Checklist checklist = checklistRepository.findById(id)
                .orElseThrow(() -> new CustomException("Checklist không tồn tại", HttpStatus.NOT_FOUND));
        checklistRepository.delete(checklist);
    }

    private void validateCreateRequest(ChecklistPostDTO request) {
        if (request == null) {
            throw new CustomException("Dữ liệu checklist không được null", HttpStatus.BAD_REQUEST);
        }
        if (isBlank(request.getName())) {
            throw new CustomException("Tên checklist không được để trống", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUpdateRequest(ChecklistDTO request) {
        if (request == null) {
            throw new CustomException("Dữ liệu checklist không được null", HttpStatus.BAD_REQUEST);
        }
        if (request.getName() != null && request.getName().isBlank()) {
            throw new CustomException("Tên checklist không hợp lệ", HttpStatus.BAD_REQUEST);
        }
    }

    private List<ChecklistTask> buildChecklistTasks(List<TaskBaseDTO> taskDtos) {
        List<ChecklistTask> result = new ArrayList<>();

        if (taskDtos == null || taskDtos.isEmpty()) {
            return result;
        }

        int order = 1;
        for (TaskBaseDTO dto : taskDtos) {
            if (dto == null) {
                continue;
            }

            ChecklistTask task = new ChecklistTask();
            task.setTitle(extractLabel(dto));
            task.setDescription(extractDescription(dto));
            task.setTaskType(mapTaskType(dto));
            task.setRequired(extractRequired(dto));
            task.setDisplayOrder(extractDisplayOrder(dto, order));
            task.setExpectedValue(extractExpectedValue(dto));
            task.setMinValue(extractMinValue(dto));
            task.setMaxValue(extractMaxValue(dto));

            result.add(task);
            order++;
        }

        return result;
    }

    private String defaultAppliesTo(String appliesTo) {
        return isBlank(appliesTo) ? "GENERAL" : appliesTo.trim().toUpperCase();
    }

    private ChecklistTask.ChecklistTaskType mapTaskType(TaskBaseDTO dto) {
        String rawType = extractTaskType(dto);

        if (rawType == null || rawType.isBlank()) {
            return ChecklistTask.ChecklistTaskType.PASS_FAIL;
        }

        String normalized = rawType.trim().toUpperCase();

        return switch (normalized) {
            case "PASS_FAIL", "CHECK" -> ChecklistTask.ChecklistTaskType.PASS_FAIL;
            case "YES_NO" -> ChecklistTask.ChecklistTaskType.YES_NO;
            case "TEXT", "SUBTASK" -> ChecklistTask.ChecklistTaskType.TEXT;
            case "NUMBER" -> ChecklistTask.ChecklistTaskType.NUMBER;
            default -> ChecklistTask.ChecklistTaskType.PASS_FAIL;
        };
    }

    private String extractLabel(TaskBaseDTO dto) {
        String value = invokeStringGetter(dto, "getLabel");
        if (isBlank(value)) {
            throw new CustomException("Task title không được để trống", HttpStatus.BAD_REQUEST);
        }
        return value.trim();
    }

    private String extractDescription(TaskBaseDTO dto) {
        return trimToNull(invokeStringGetter(dto, "getDescription"));
    }

    private String extractTaskType(TaskBaseDTO dto) {
        Object value = invokeGetter(dto, "getTaskType");
        return value == null ? null : value.toString();
    }

    private boolean extractRequired(TaskBaseDTO dto) {
        Object value = invokeGetter(dto, "getRequired");
        if (value instanceof Boolean b) {
            return b;
        }

        value = invokeGetter(dto, "isRequired");
        if (value instanceof Boolean b) {
            return b;
        }

        value = invokeGetter(dto, "getRequiredTask");
        if (value instanceof Boolean b) {
            return b;
        }

        return true;
    }

    private Integer extractDisplayOrder(TaskBaseDTO dto, int defaultValue) {
        Object value = invokeGetter(dto, "getDisplayOrder");
        if (value instanceof Integer i && i > 0) {
            return i;
        }

        value = invokeGetter(dto, "getSortOrder");
        if (value instanceof Integer i && i > 0) {
            return i;
        }

        return defaultValue;
    }

    private String extractExpectedValue(TaskBaseDTO dto) {
        return trimToNull(invokeStringGetter(dto, "getExpectedValue"));
    }

    private Double extractMinValue(TaskBaseDTO dto) {
        Object value = invokeGetter(dto, "getMinValue");
        return value instanceof Double d ? d : null;
    }

    private Double extractMaxValue(TaskBaseDTO dto) {
        Object value = invokeGetter(dto, "getMaxValue");
        return value instanceof Double d ? d : null;
    }

    private Object invokeGetter(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String invokeStringGetter(Object target, String methodName) {
        Object value = invokeGetter(target, methodName);
        return value == null ? null : value.toString();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}