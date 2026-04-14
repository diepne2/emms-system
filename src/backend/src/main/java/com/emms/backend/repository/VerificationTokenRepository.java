package com.emms.backend.repository;

import java.util.ArrayList;
import com.emms.backend.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findVerificationTokenEntityByToken(String token);

    ArrayList<VerificationToken> findAllVerificationTokenEntityByUser(User user);
    
}
