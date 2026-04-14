package com.emms.backend.repository;


import com.emms.backend.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CheckListRepository extends JpaRepository<Checklist, Long> {

    long countByActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    @Query("""
           SELECT CASE WHEN COUNT(c) > :threshold THEN true ELSE false END
           FROM Checklist c
           """)
    boolean hasMoreThan(@Param("threshold") Long threshold);
}