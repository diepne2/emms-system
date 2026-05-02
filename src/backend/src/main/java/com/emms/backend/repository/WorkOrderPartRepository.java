package com.emms.backend.repository;

import com.emms.backend.entity.WorkOrderPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderPartRepository extends JpaRepository<WorkOrderPart, Long> {

    List<WorkOrderPart> findByWorkOrder_Id(Long workOrderId);

}