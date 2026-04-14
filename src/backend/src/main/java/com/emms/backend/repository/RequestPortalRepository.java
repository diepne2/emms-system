package com.emms.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.emms.backend.entity.RequestPortal;
public interface RequestPortalRepository extends JpaRepository<RequestPortal, Long> , JpaSpecificationExecutor<RequestPortal>{
    Optional<RequestPortal> findByUuid(String uuid);
    
}
