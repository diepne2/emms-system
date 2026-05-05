package com.emms.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.Role;
import com.emms.backend.entity.enums.RoleCode;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Optional<Role> findByCode(RoleCode code);
    
}
