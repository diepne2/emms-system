package com.emms.backend.repository;

import com.emms.backend.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Request> findByCreatedAtBetweenOrUpdatedAtBetween(
            LocalDateTime createdStart,
            LocalDateTime createdEnd,
            LocalDateTime updatedStart,
            LocalDateTime updatedEnd
    );
}