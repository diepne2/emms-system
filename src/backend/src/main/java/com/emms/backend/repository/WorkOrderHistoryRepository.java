package com.emms.backend.repository;

import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.WorkOrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkOrderHistoryRepository extends JpaRepository<WorkOrderHistory, Long> {

    List<WorkOrderHistory> findByWorkOrderOrderByVersionNoDescCreatedAtDesc(WorkOrder workOrder);

    Optional<WorkOrderHistory> findTopByWorkOrderOrderByVersionNoDesc(WorkOrder workOrder);

    List<WorkOrderHistory> findByWorkOrder_StatusInOrderByCreatedAtDesc(
            List<WorkOrder.WorkOrderStatus> statuses
    );
}