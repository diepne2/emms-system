package com.emms.backend.entity;

import com.emms.backend.entity.abstracts.Audit;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "user_invitations", indexes = {
        @Index(name = "idx_invitation_email", columnList = "email")
})
public class UserInvitation extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long invitationId;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // ===== Constructor =====

    public UserInvitation() {
    }

    public UserInvitation(String email, Role role) {
        setEmail(email);
        setRole(role);
    }

    // ===== Getter Setter =====

    public Long getInvitationId() {
        return invitationId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = trim(email);
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role không được null");
        }
        this.role = role;
    }

    // ===== Utils =====

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public void setToken(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setToken'");
    }

    public void setUsed(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setUsed'");
    }
}