package com.emms.backend.service;

import com.emms.backend.dto.email.EmailAttachmentDTO;
import com.emms.backend.entity.User;
import com.emms.backend.exception.CustomException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional
public class EmailService implements MailService {

    private final JavaMailSender emailSender;
    private final MailProperties mailProperties;
    private final Environment environment;
    private final SpringTemplateEngine thymeleafTemplateEngine;

    @Value("${mail.enable:false}")
    private Boolean enableEmails;

    @Value("${mail.recipients:}")
    private String[] recipients;

    public EmailService(JavaMailSender emailSender,
                        MailProperties mailProperties,
                        Environment environment,
                        SpringTemplateEngine thymeleafTemplateEngine) {
        this.emailSender = emailSender;
        this.mailProperties = mailProperties;
        this.environment = environment;
        this.thymeleafTemplateEngine = thymeleafTemplateEngine;
    }

    @Override
    public void sendSimpleMessage(String[] to, String subject, String text) {
        String[] validRecipients = normalizeRecipients(to);
        if (shouldSkipSendingMail() || validRecipients.length == 0) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(validRecipients);
            message.setSubject(safe(subject));
            message.setText(safe(text));
            message.setFrom(resolveFromEmailOrDefault());
            emailSender.send(message);
        } catch (MailException ex) {
            throw new CustomException("Không thể gửi email thường", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void sendMessageWithAttachment(String to,
                                          String subject,
                                          String text,
                                          String attachmentName,
                                          byte[] attachmentData,
                                          String attachmentType) {
        if (shouldSkipSendingMail() || to == null || to.isBlank()) {
            return;
        }

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            applyFrom(helper);
            helper.setTo(to.trim());
            helper.setSubject(safe(subject));
            helper.setText(safe(text), false);

            if (attachmentData != null
                    && attachmentData.length > 0
                    && attachmentName != null
                    && !attachmentName.isBlank()) {
                helper.addAttachment(
                        attachmentName.trim(),
                        new ByteArrayDataSource(
                                attachmentData,
                                safeContentType(attachmentType)
                        )
                );
            }

            emailSender.send(message);
        } catch (MessagingException | MailException ex) {
            throw new CustomException("Không thể gửi email có file đính kèm", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Async
    public void sendMessageUsingThymeleafTemplate(String[] to,
                                                  String subject,
                                                  Map<String, Object> templateModel,
                                                  String template,
                                                  Locale locale,
                                                  List<EmailAttachmentDTO> attachmentDtos) {
        String[] validRecipients = normalizeRecipients(to);
        if (shouldSkipSendingMail() || validRecipients.length == 0) {
            return;
        }

        if (template == null || template.isBlank()) {
            throw new CustomException("Template email không được để trống", HttpStatus.BAD_REQUEST);
        }

        Locale resolvedLocale = locale != null ? locale : Locale.getDefault();
        Context context = new Context(resolvedLocale);

        if (templateModel != null && !templateModel.isEmpty()) {
            context.setVariables(templateModel);
        }

        context.setVariable("environment", environment);
        context.setVariable("brandName", "EMMS System");
        context.setVariable("backgroundColor", "#f8f9fa");

        try {
            String htmlBody = thymeleafTemplateEngine.process(template.trim(), context);
            sendHtmlMessage(validRecipients, subject, htmlBody, attachmentDtos);
        } catch (Exception ex) {
            throw new CustomException("Không thể gửi email template", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void sendHtmlMessage(String[] to,
                                String subject,
                                String htmlBody,
                                List<EmailAttachmentDTO> attachmentDtos) throws MessagingException {
        String[] validRecipients = normalizeRecipients(to);
        if (shouldSkipSendingMail() || validRecipients.length == 0) {
            return;
        }

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                true,
                StandardCharsets.UTF_8.name()
        );

        applyFrom(helper);
        helper.setTo(validRecipients);
        helper.setSubject(safe(subject));
        helper.setText(safe(htmlBody), true);

        if (attachmentDtos != null && !attachmentDtos.isEmpty()) {
            for (EmailAttachmentDTO attachmentDto : attachmentDtos) {
                if (attachmentDto == null) {
                    continue;
                }
                if (attachmentDto.getData() == null || attachmentDto.getData().length == 0) {
                    continue;
                }
                if (attachmentDto.getFileName() == null || attachmentDto.getFileName().isBlank()) {
                    continue;
                }

                helper.addAttachment(
                        attachmentDto.getFileName().trim(),
                        new ByteArrayDataSource(
                                attachmentDto.getData(),
                                safeContentType(attachmentDto.getContentType())
                        )
                );
            }
        }

        emailSender.send(message);
    }

    @Override
    public void sendMailToSuperAdmins(String subject, String text) {
        String[] validRecipients = normalizeRecipients(recipients);
        if (validRecipients.length == 0 || shouldSkipSendingMail()) {
            return;
        }

        try {
            String htmlBody = safe(text).replace("\n", "<br>");
            sendHtmlMessage(validRecipients, subject, htmlBody, null);
        } catch (MessagingException ex) {
            throw new CustomException("Không thể gửi email cho super admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Async
    public void removeUserFromContactList(String email) {
        // Chưa dùng trong EMMS
    }

    @Override
    @Async
    public void addToContactList(User user) {
        // Chưa dùng trong EMMS
    }

    private boolean shouldSkipSendingMail() {
        return Boolean.FALSE.equals(enableEmails) || Boolean.TRUE.equals(MailService.skipMail.get());
    }

    private String[] normalizeRecipients(String[] to) {
        if (to == null || to.length == 0) {
            return new String[0];
        }

        return Arrays.stream(to)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toArray(String[]::new);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeContentType(String contentType) {
        return (contentType == null || contentType.isBlank())
                ? "application/octet-stream"
                : contentType.trim();
    }

    private String resolveFromEmailOrDefault() {
        String fromEmail = mailProperties.getUsername();
        if (fromEmail != null && !fromEmail.isBlank()) {
            return fromEmail.trim();
        }
        return "no-reply@localhost";
    }

    private void applyFrom(MimeMessageHelper helper) throws MessagingException {
        String fromEmail = resolveFromEmailOrDefault();
        String fromName = "EMMS System";

        try {
            helper.setFrom(new InternetAddress(fromEmail, fromName));
        } catch (UnsupportedEncodingException ex) {
            helper.setFrom(fromEmail);
        }
    }
}