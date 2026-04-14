package com.emms.backend.service;

import com.emms.backend.entity.UserInvitation;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.UserInvitationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserInvitationService {

    private final UserInvitationRepository userInvitationRepository;

    public UserInvitationService(UserInvitationRepository userInvitationRepository) {
        this.userInvitationRepository = userInvitationRepository;
    }

    public UserInvitation create(UserInvitation userInvitation) {
        if (userInvitation == null) {
            throw new CustomException("Dữ liệu lời mời không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        return userInvitationRepository.save(userInvitation);
    }

    public List<UserInvitation> getAll() {
        return userInvitationRepository.findAll();
    }

    public UserInvitation getById(Long id) {
        return userInvitationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy lời mời", HttpStatus.NOT_FOUND));
    }

    public void delete(Long id) {
        if (!userInvitationRepository.existsById(id)) {
            throw new CustomException("Không tìm thấy lời mời", HttpStatus.NOT_FOUND);
        }
        userInvitationRepository.deleteById(id);
    }

    public List<UserInvitation> findByRoleAndEmail(Long roleId, String email) {
        if (roleId == null) {
            throw new CustomException("roleId không được null", HttpStatus.BAD_REQUEST);
        }
        if (email == null || email.isBlank()) {
            throw new CustomException("email không được để trống", HttpStatus.BAD_REQUEST);
        }

        return userInvitationRepository.findByRole_RoleIdAndEmailIgnoreCase(roleId, email.trim());
    }
}