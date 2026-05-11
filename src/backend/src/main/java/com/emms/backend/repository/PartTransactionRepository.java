package com.emms.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.PartTransaction;

public interface PartTransactionRepository
        extends JpaRepository<PartTransaction, Long> {

    List<PartTransaction> findByPartIdOrderByCreatedAtDesc(Long partId);
}