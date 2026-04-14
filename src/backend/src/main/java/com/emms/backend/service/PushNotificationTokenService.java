package com.emms.backend.service;

import com.emms.backend.exception.CustomException;
import com.emms.backend.dto.chat.ChatMessageDTO;
import com.emms.backend.entity.PushNotificationToken;
import com.emms.backend.repository.PushNotificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PushNotificationTokenService {

    private final PushNotificationTokenRepository pushNotificationTokenRepository;

    // ================= CREATE / UPSERT =================
    public PushNotificationToken create(PushNotificationToken token) {
        if (token == null) {
            throw new CustomException("Token is null", HttpStatus.BAD_REQUEST);
        }

        // Nếu token đã tồn tại → update user/device
        Optional<PushNotificationToken> existing =
                pushNotificationTokenRepository.findAll()
                        .stream()
                        .filter(t -> t.getToken().equals(token.getToken()))
                        .findFirst();

        if (existing.isPresent()) {
            PushNotificationToken entity = existing.get();

            entity.setUser(token.getUser());
            entity.setDeviceName(token.getDeviceName());
            entity.setDeviceType(token.getDeviceType());
            entity.setActive(true);
            entity.markUsed();

            return pushNotificationTokenRepository.save(entity);
        }

        return pushNotificationTokenRepository.save(token);
    }

    // ================= FIND =================
    public Optional<PushNotificationToken> findByUser(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return pushNotificationTokenRepository.findByUser_Id(userId);
    }

    public PushNotificationToken findRequiredByUser(Long userId) {
        return pushNotificationTokenRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new CustomException("Push token not found for user", HttpStatus.NOT_FOUND));
    }

    // ================= UPDATE =================
    public PushNotificationToken update(PushNotificationToken token) {
        if (token == null || token.getPushNotificationTokenId() == null) {
            throw new CustomException("Invalid token", HttpStatus.BAD_REQUEST);
        }

        PushNotificationToken existing = pushNotificationTokenRepository.findById(token.getPushNotificationTokenId())
                .orElseThrow(() ->
                        new CustomException("Token not found", HttpStatus.NOT_FOUND));

        existing.setDeviceName(token.getDeviceName());
        existing.setDeviceType(token.getDeviceType());
        existing.setActive(token.isActive());

        existing.markUsed();

        return pushNotificationTokenRepository.save(existing);
    }

    // ================= DELETE =================
    public void delete(Long id) {
        if (id == null || !pushNotificationTokenRepository.existsById(id)) {
            throw new CustomException("Token not found", HttpStatus.NOT_FOUND);
        }
        pushNotificationTokenRepository.deleteById(id);
    }

    // ================= SOFT DELETE =================
    public void deactivateByUser(Long userId) {
        PushNotificationToken token = findRequiredByUser(userId);
        token.setActive(false);
        pushNotificationTokenRepository.save(token);
    }

    // ================= MARK USED =================
    public void markUsed(Long userId) {
        Optional<PushNotificationToken> optional = findByUser(userId);
        if (optional.isPresent()) {
            PushNotificationToken token = optional.get();
            token.markUsed();
            pushNotificationTokenRepository.save(token);
        }
    }

    public void pushChatToUser(String username, Long id, ChatMessageDTO dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pushChatToUser'");
    }
}