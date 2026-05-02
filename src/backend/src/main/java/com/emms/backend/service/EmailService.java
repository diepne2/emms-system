package com.emms.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(@Nullable JavaMailSender mailSender,
                        @Nullable SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendMessageUsingThymeleafTemplate(
            String[] to,
            String subject,
            Map<String, Object> variables,
            String template,
            Locale locale
    ) {
        if (mailSender == null || templateEngine == null) {
            log.debug("Không tìm thấy mail sender hoặc template engine, bỏ qua gửi email.");
            return;
        }

        try {
            Context context = new Context(locale == null ? Locale.getDefault() : locale);
            if (variables != null) {
                context.setVariables(variables);
            }

            String html = templateEngine.process(template, context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            log.error("Gửi email thất bại: {}", ex.getMessage(), ex);
            throw new RuntimeException("Gửi email thất bại", ex);
        }
    }
}