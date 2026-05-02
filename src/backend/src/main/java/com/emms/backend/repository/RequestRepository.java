package com.emms.backend.repository;

import com.emms.backend.entity.Request;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @Override
    @EntityGraph(attributePaths = {"location", "workOrder"})
    List<Request> findAll();

    List<Request> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Request> findByCreatedAtBetweenOrUpdatedAtBetween(
            LocalDateTime start1,
            LocalDateTime end1,
            LocalDateTime start2,
            LocalDateTime end2
    );
}