package com.emms.backend.repository;

import com.emms.backend.entity.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReadingRepository extends JpaRepository<Reading, Long> {

    Optional<Reading> findTopByMeterIdOrderByRecordedAtDescIdDesc(Long meterId);

    Optional<Reading> findTopByMeterIdAndRecordedAtBeforeOrderByRecordedAtDescIdDesc(Long meterId, LocalDateTime recordedAt);

    List<Reading> findByMeterIdOrderByRecordedAtDescIdDesc(Long meterId);

    List<Reading> findByMeterIdAndRecordedAtBetweenOrderByRecordedAtAscIdAsc(
            Long meterId,
            LocalDateTime from,
            LocalDateTime to
    );
}