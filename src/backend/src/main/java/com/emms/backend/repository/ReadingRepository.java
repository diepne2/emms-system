package com.emms.backend.repository;

import com.emms.backend.entity.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ReadingRepository extends JpaRepository<Reading, Long> {

    Collection<Reading> findByMeter_Id(Long meterId);

    Optional<Reading> findTopByMeter_IdOrderByRecordedAtDesc(Long meterId);

    Collection<Reading> findByMeter_IdOrderByRecordedAtDesc(Long meterId);
}