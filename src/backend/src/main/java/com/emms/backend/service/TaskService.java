package com.emms.backend.service;

import com.emms.backend.dto.task.TaskDTO;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.TaskMapper;
import com.emms.backend.entity.Task;
import com.emms.backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final WorkOrderService workOrderService;
    private final FileService fileService;
    private final TaskMapper taskMapper;
    private final EntityManager em;

    public Task create(Task task) {
        Task savedTask = taskRepository.saveAndFlush(task);
        em.refresh(savedTask);
        return savedTask;
    }

    public Task update(Long id, TaskDTO dto) {
        Task savedTask = taskRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy tác vụ", HttpStatus.NOT_FOUND));

        taskMapper.updateTask(savedTask, dto);

        Task updatedTask = taskRepository.saveAndFlush(savedTask);
        em.refresh(updatedTask);
        return updatedTask;
    }

    @Transactional(readOnly = true)
    public Collection<Task> getAll() {
        return taskRepository.findAll();
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new CustomException("Không tìm thấy tác vụ", HttpStatus.NOT_FOUND);
        }
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Task> findByWorkOrder(Long id) {
        return taskRepository.findByWorkOrder_IdOrderByCreatedAtAsc(id);
    }

    @Transactional(readOnly = true)
    public List<Task> findByPreventiveMaintenance(Long id) {
        return taskRepository.findByPreventiveMaintenance_Id(id);
    }

    @Transactional(readOnly = true)
    public Task findEntityById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy tác vụ", HttpStatus.NOT_FOUND));
    }
}