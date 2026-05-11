package com.emms.backend.repository;

import com.emms.backend.entity.PartTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PartTransactionRepository extends JpaRepository<PartTransaction, Long> {

    List<PartTransaction> findByPartIdOrderByCreatedAtDesc(Long partId);

    @Query("""
            SELECT t
            FROM PartTransaction t
            WHERE (:type IS NULL OR LOWER(t.type) = LOWER(:type))
              AND (
                    :keyword IS NULL
                    OR LOWER(t.type) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(t.note) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR CAST(t.partId AS string) LIKE CONCAT('%', :keyword, '%')
                    OR CAST(t.workOrderId AS string) LIKE CONCAT('%', :keyword, '%')
              )
            ORDER BY t.createdAt DESC
            """)
    List<PartTransaction> searchTransactions(
            @Param("keyword") String keyword,
            @Param("type") String type
    );
}