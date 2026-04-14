package com.emms.backend.repository;

import com.emms.backend.entity.Location;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findAllByOrderByNameAsc();

    List<Location> findAll(Sort sort);

    List<Location> findByParentLocation(String parentLocation);

    List<Location> findByParentLocation(String parentLocation, Sort sort);

    List<Location> findByNameIgnoreCase(String name);

    Optional<Location> findByLocationId(Long locationId);

    List<Location> findByLocationIdIn(List<Long> ids);

    long countByParentLocation(String parentLocation);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndLocationIdNot(String name, Long locationId);
}