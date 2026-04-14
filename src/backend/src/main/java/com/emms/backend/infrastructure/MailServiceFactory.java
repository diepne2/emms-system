package com.emms.backend.infrastructure;

import com.emms.backend.entity.enums.MailType;
import com.emms.backend.service.EmailService;
import com.emms.backend.service.MailService;
import com.emms.backend.service.SendgridService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class MailServiceFactory {
    @Value("${mail.type:SMTP}")
    private MailType mailType;

    private final EmailService emailService;
    private final SendgridService sendgridService;

    public MailService getMailService() {
        switch (mailType) {
            case SENDGRID:
                return sendgridService;
            default:
                return emailService;
        }
    }
}