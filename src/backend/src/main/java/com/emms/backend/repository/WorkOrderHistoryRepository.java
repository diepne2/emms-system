package com.emms.backend.repository;

import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.WorkOrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WorkOrderHistoryRepository extends JpaRepository<WorkOrderHistory, Long> {

    Collection<WorkOrderHistory> findByWorkOrder(WorkOrder workOrder);
}