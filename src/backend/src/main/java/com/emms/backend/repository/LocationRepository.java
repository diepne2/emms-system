package com.emms.backend.repository;

import com.emms.backend.entity.Location;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findAllByOrderByNameAsc();

    List<Location> findAll(Sort sort);

    List<Location> findByParentLocation(String parentLocation);

    List<Location> findByParentLocation(String parentLocation, Sort sort);

    Optional<Location> findByNameIgnoreCase(String name);

    List<Location> findByIdIn(List<Long> ids);

    long countByParentLocation(String parentLocation);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
        SELECT l FROM Location l
        WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(l.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(l.parentLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY
            CASE
                WHEN LOWER(l.name) = LOWER(:keyword) THEN 0
                WHEN LOWER(l.address) = LOWER(:keyword) THEN 1
                WHEN LOWER(l.parentLocation) = LOWER(:keyword) THEN 2
                WHEN LOWER(l.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 3
                ELSE 4
            END,
            l.name ASC
    """)
    List<Location> searchByKeyword(@Param("keyword") String keyword);
}