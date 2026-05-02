package com.emms.backend.service;

import com.emms.backend.exception.CustomException;
import com.emms.backend.entity.User;
import com.emms.backend.entity.VerificationToken;
import com.emms.backend.repository.UserRepository;
import com.emms.backend.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public VerificationToken getVerificationTokenEntity(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return verificationTokenRepository.findVerificationTokenEntityByToken(token.trim());
    }

    public void deleteVerificationTokenEntity(User user) {
        if (user == null) {
            return;
        }

        List<VerificationToken> verificationTokens =
                verificationTokenRepository.findAllVerificationTokenEntityByUser(user);

        if (verificationTokens != null && !verificationTokens.isEmpty()) {
            verificationTokenRepository.deleteAll(verificationTokens);
        }
    }

    @Transactional(readOnly = true)
    private VerificationToken verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new CustomException("Token thì bắt buộc", HttpStatus.BAD_REQUEST);
        }

        VerificationToken verificationToken =
                verificationTokenRepository.findVerificationTokenEntityByToken(token.trim());

        if (verificationToken == null) {
            throw new CustomException("Liên kết kích hoạt không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getExpiryDate() == null) {
            throw new CustomException("Ngày hết hạn của token bị thiếu", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CustomException("Liên kết kích hoạt đã hết hạn", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getUser() == null) {
            throw new CustomException("Token không thuộc về bất kỳ người dùng nào", HttpStatus.BAD_REQUEST);
        }

        return verificationToken;
    }

    public String confirmMail(String token) {
        VerificationToken verificationToken = verifyToken(token);
        User user = verificationToken.getUser();

        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
        return user.getEmail();
    }

    public User confirmResetPassword(String token) {
        VerificationToken verificationToken = verifyToken(token);
        User user = verificationToken.getUser();

        if (verificationToken.getPayload() == null || verificationToken.getPayload().trim().isEmpty()) {
            throw new CustomException("Payload để đặt lại mật khẩu không được để trống", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(verificationToken.getPayload()));
        User savedUser = userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
        return savedUser;
    }
}