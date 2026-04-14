package com.emms.backend.infrastructure;

import com.emms.backend.service.EmailService;
import org.springframework.stereotype.Component;

@Component
public class MailServiceFactory {

    private final EmailService emailService;

    public MailServiceFactory(EmailService emailService) {
        this.emailService = emailService;
    }

    public EmailService getMailService() {
        return emailService;
    }
}