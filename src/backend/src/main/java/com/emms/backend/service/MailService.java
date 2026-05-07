package com.emms.backend.service;

import com.emms.backend.dto.email.EmailAttachmentDTO;
import com.emms.backend.entity.User;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface MailService {

    ThreadLocal<Boolean> skipMail = ThreadLocal.withInitial(() -> false);

    void sendSimpleMessage(String[] to, String subject, String text);

    void sendMessageWithAttachment(
            String to,
            String subject,
            String text,
            String attachmentName,
            byte[] attachmentData,
            String attachmentType
    );

    void sendMessageUsingThymeleafTemplate(
            String[] to,
            String subject,
            Map<String, Object> templateModel,
            String template,
            Locale locale,
            List<EmailAttachmentDTO> attachmentDTOS
    );

    void sendHtmlMessage(
            String[] to,
            String subject,
            String htmlBody,
            List<EmailAttachmentDTO> attachmentDTOS
    ) throws IOException;

    void sendMailToSuperAdmins(String subject, String text);

    void addToContactList(User user);

    void sendInviteEmail(
            String to,
            String username,
            String tempPassword,
            String roleName,
            String loginLink
    );

    void sendResetPasswordEmail(
            String to,
            String usernameOrEmail,
            String resetLink
    );
}