package com.emms.backend.repository;

import com.emms.backend.entity.Checklist;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CheckListRepository extends JpaRepository<Checklist, Long> {

    long countByActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    @EntityGraph(attributePaths = "tasks")
    Optional<Checklist> findWithTasksById(Long id);

    @EntityGraph(attributePaths = "tasks")
    List<Checklist> findAll(Sort sort);

    @Query("""
        SELECT DISTINCT c
        FROM Checklist c
        LEFT JOIN FETCH c.tasks t
        WHERE LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(COALESCE(c.appliesTo, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY c.id DESC
    """)
    List<Checklist> search(@Param("keyword") String keyword);
}