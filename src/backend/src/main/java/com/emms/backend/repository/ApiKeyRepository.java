package com.emms.backend.repository;

import com.emms.backend.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long>, JpaSpecificationExecutor<ApiKey> {

    Optional<ApiKey> findByCode(String code);

    @Modifying
    @Transactional
    @Query("UPDATE ApiKey a SET a.lastUsed = :lastUsed WHERE a.apiKeyId = :apiKeyId")
    int updateLastUsed(@Param("apiKeyId") Long apiKeyId,
                       @Param("lastUsed") LocalDateTime lastUsed);
}