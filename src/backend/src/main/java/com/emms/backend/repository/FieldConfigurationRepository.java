package com.emms.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.FieldConfiguration;
import com.emms.backend.entity.enums.WorkOrderFieldKey;

import java.util.List;
import java.util.Optional;

public interface FieldConfigurationRepository extends JpaRepository<FieldConfiguration, Long> {

    // Lấy tất cả field theo WorkOrderConfiguration
    List<FieldConfiguration> findByWorkOrderConfiguration_Id(Long configId);

    // Tìm theo key + config (dùng cho validate unique)
    Optional<FieldConfiguration> findByWorkOrderConfiguration_IdAndFieldKey(
            Long configId,
            WorkOrderFieldKey fieldKey
    );

    // Check tồn tại (rất hay dùng khi create/update)
    boolean existsByWorkOrderConfiguration_IdAndFieldKey(
            Long configId,
            WorkOrderFieldKey fieldKey
    );

    // Check tồn tại nhưng exclude id (update case)
    boolean existsByWorkOrderConfiguration_IdAndFieldKeyAndIdNot(
            Long configId,
            WorkOrderFieldKey fieldKey,
            Long id
    );
}