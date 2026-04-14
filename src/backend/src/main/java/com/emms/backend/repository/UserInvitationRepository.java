package com.emms.backend.repository;

import com.emms.backend.entity.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {

    List<UserInvitation> findByRole_RoleIdAndEmailIgnoreCase(Long roleId, String email);
}