package com.emms.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.emms.backend.dto.email.EmailAttachmentDTO;
import com.emms.backend.entity.User;
import com.emms.backend.exception.CustomException;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class SendgridService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(SendgridService.class);

    private final SpringTemplateEngine thymeleafTemplateEngine;
    private final Environment environment;
    private final ObjectMapper objectMapper;

    @Value("${sendgrid.api.key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email:no-reply@example.com}")
    private String fromEmail;

    @Value("${api.host:}")
    private String apiHost;

    @Value("${frontend.url:}")
    private String frontendUrl;

    @Value("${mail.recipients:}")
    private String[] recipients;

    @Value("${mail.enable:false}")
    private Boolean enableEmails;

    @Value("${cloud-version:false}")
    private boolean cloudVersion;

    @Value("${sendgrid.contact-list-id:}")
    private String contactListId;

    private String fromName;

    public SendgridService(SpringTemplateEngine thymeleafTemplateEngine,
                           Environment environment,
                           ObjectMapper objectMapper) {
        this.thymeleafTemplateEngine = thymeleafTemplateEngine;
        this.environment = environment;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        if (fromName == null || fromName.isBlank()) {
            fromName = "EMMS";
        }
    }

    @Override
    @Async
    public void addToContactList(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Skip adding contact to SendGrid because user/email is null.");
            return;
        }

        if (shouldSkipSendingEmail() || !cloudVersion) {
            return;
        }

        if (contactListId == null || contactListId.isBlank()) {
            log.warn("Skip adding contact to SendGrid because contactListId is blank.");
            return;
        }

        try {
            SendGrid sendGrid = buildClient();

            Request request = new Request();
            request.setMethod(Method.PUT);
            request.setEndpoint("marketing/contacts");

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 15);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            String trialEndDate = dateFormat.format(calendar.getTime());

            Map<String, Object> contact = new HashMap<>();
            contact.put("email", safeTrim(user.getEmail()));
            contact.put("first_name", safeTrim(user.getFirstName()));
            contact.put("last_name", safeTrim(user.getLastName()));
            contact.put("trial_end_date", trialEndDate);

            Map<String, Object> body = new HashMap<>();
            body.put("contacts", Collections.singletonList(contact));
            body.put("list_ids", Collections.singletonList(contactListId));

            request.setBody(objectMapper.writeValueAsString(body));

            Response response = sendGrid.api(request);
            ensureSuccess(response, "Failed to add user to SendGrid contacts");

            log.info("User added to SendGrid contact list successfully: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error adding user to SendGrid contacts", e);
            throw new CustomException(
                    "Failed to add user to SendGrid contacts",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Async
    public void removeUserFromContactList(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            log.warn("Skip removing contact from SendGrid because email is blank.");
            return;
        }

        if (shouldSkipSendingEmail() || !cloudVersion) {
            return;
        }

        if (contactListId == null || contactListId.isBlank()) {
            log.warn("Skip removing contact from SendGrid because contactListId is blank.");
            return;
        }

        try {
            SendGrid sendGrid = buildClient();

            String safeEmail = userEmail.replace("'", "\\'");

            Request searchRequest = new Request();
            searchRequest.setMethod(Method.POST);
            searchRequest.setEndpoint("marketing/contacts/search");

            Map<String, Object> searchBody = new HashMap<>();
            searchBody.put("query", "email = '" + safeEmail + "'");
            searchRequest.setBody(objectMapper.writeValueAsString(searchBody));

            Response searchResponse = sendGrid.api(searchRequest);
            if (searchResponse.getStatusCode() >= 400) {
                log.error(
                        "SendGrid search failed for {}: status={}, body={}",
                        userEmail,
                        searchResponse.getStatusCode(),
                        searchResponse.getBody()
                );
                return;
            }

            JsonNode responseNode = objectMapper.readTree(searchResponse.getBody());
            JsonNode resultNode = responseNode.get("result");

            if (resultNode == null || !resultNode.isArray() || resultNode.isEmpty()) {
                log.warn("Contact email not found in SendGrid: {}", userEmail);
                return;
            }

            JsonNode first = resultNode.get(0);
            JsonNode idNode = first.get("id");
            if (idNode == null || idNode.asText().isBlank()) {
                log.warn("Contact id missing for email {}", userEmail);
                return;
            }

            String contactId = idNode.asText();

            Request removeRequest = new Request();
            removeRequest.setMethod(Method.DELETE);
            removeRequest.setEndpoint("marketing/lists/" + contactListId + "/contacts");
            removeRequest.addQueryParam("contact_ids", contactId);

            Response removeResponse = sendGrid.api(removeRequest);
            if (removeResponse.getStatusCode() >= 400) {
                log.error(
                        "SendGrid list removal failed for {}: status={}, body={}",
                        userEmail,
                        removeResponse.getStatusCode(),
                        removeResponse.getBody()
                );
                return;
            }

            log.info("User {} removed from SendGrid list {} successfully.", userEmail, contactListId);
        } catch (Exception e) {
            log.error("Unexpected error removing user {} from SendGrid list", userEmail, e);
        }
    }

    @Override
    public void sendSimpleMessage(String[] to, String subject, String text) {
        if (shouldSkipSendingEmail()) {
            return;
        }

        String[] validRecipients = sanitizeRecipients(to);
        if (validRecipients.length == 0) {
            log.warn("Skip sending simple email because recipient list is empty.");
            return;
        }

        try {
            Email from = new Email(fromEmail, fromName);
            Content content = new Content("text/plain", text == null ? "" : text);

            Mail mail = new Mail();
            mail.setFrom(from);
            mail.setSubject(subject == null ? "" : subject);
            mail.addContent(content);

            Personalization personalization = new Personalization();
            for (String recipient : validRecipients) {
                personalization.addTo(new Email(recipient));
            }
            mail.addPersonalization(personalization);

            Response response = sendMail(mail);
            ensureSuccess(response, "Failed to send email");

            log.info("Simple email sent successfully. Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Error sending email via SendGrid", e);
            throw new CustomException("Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void sendMessageWithAttachment(String to,
                                          String subject,
                                          String text,
                                          String attachmentName,
                                          byte[] attachmentData,
                                          String attachmentType) {
        if (shouldSkipSendingEmail()) {
            return;
        }

        if (to == null || to.isBlank()) {
            throw new CustomException("Recipient email must not be blank", HttpStatus.BAD_REQUEST);
        }

        try {
            Email from = new Email(fromEmail, fromName);
            Email recipient = new Email(to.trim());
            Content content = new Content("text/plain", text == null ? "" : text);
            Mail mail = new Mail(from, subject == null ? "" : subject, recipient, content);

            if (attachmentData != null && attachmentData.length > 0) {
                Attachments attachments = new Attachments();
                attachments.setContent(Base64.getEncoder().encodeToString(attachmentData));
                attachments.setType(defaultIfBlank(attachmentType, "application/octet-stream"));
                attachments.setFilename(defaultIfBlank(attachmentName, "attachment"));
                attachments.setDisposition("attachment");
                mail.addAttachments(attachments);
            }

            Response response = sendMail(mail);
            ensureSuccess(response, "Failed to send email with attachment");

            log.info("Email with attachment sent successfully. Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Error sending email with attachment via SendGrid", e);
            throw new CustomException(
                    "Failed to send email with attachment",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    @Async
    public void sendMessageUsingThymeleafTemplate(String[] to,
                                                  String subject,
                                                  Map<String, Object> templateModel,
                                                  String template,
                                                  Locale locale,
                                                  List<EmailAttachmentDTO> attachmentDTOS) {
        if (shouldSkipSendingEmail()) {
            return;
        }

        String[] validRecipients = sanitizeRecipients(to);
        if (validRecipients.length == 0) {
            log.warn("Skip sending Thymeleaf email because recipient list is empty.");
            return;
        }

        Context thymeleafContext = new Context();
        thymeleafContext.setLocale(locale == null ? Locale.getDefault() : locale);
        thymeleafContext.setVariables(templateModel == null ? Map.of() : templateModel);
        thymeleafContext.setVariable("environment", environment);
        thymeleafContext.setVariable("apiHost", apiHost);
        thymeleafContext.setVariable("frontendUrl", frontendUrl);

        String htmlBody = thymeleafTemplateEngine.process(template, thymeleafContext);

        try {
            sendHtmlMessage(validRecipients, subject, htmlBody, attachmentDTOS, template);
        } catch (IOException e) {
            log.error("Error sending templated email", e);
            throw new CustomException("Can't send the mail", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void sendHtmlMessage(String[] to,
                                String subject,
                                String htmlBody,
                                List<EmailAttachmentDTO> attachmentDTOS) throws IOException {
        sendHtmlMessage(to, subject, htmlBody, attachmentDTOS, null);
    }

    private void sendHtmlMessage(String[] to,
                                 String subject,
                                 String htmlBody,
                                 List<EmailAttachmentDTO> attachmentDTOS,
                                 String template) throws IOException {
        if (shouldSkipSendingEmail()) {
            return;
        }

        String[] validRecipients = sanitizeRecipients(to);
        if (validRecipients.length == 0) {
            log.warn("Skip sending html email because recipient list is empty.");
            return;
        }

        boolean allDemoRecipients = Arrays.stream(validRecipients)
                .allMatch(recipient -> recipient.toLowerCase(Locale.ROOT).endsWith("@demo.com"));
        if (allDemoRecipients) {
            log.info("Skip sending html email because all recipients are demo accounts.");
            return;
        }

        Email from = new Email(fromEmail, fromName);
        Content content = new Content("text/html", htmlBody == null ? "" : htmlBody);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject == null ? "" : subject);
        mail.addContent(content);

        if (template != null && !template.isBlank()) {
            mail.addCategory(template);
        }

        Personalization personalization = new Personalization();
        for (String recipient : validRecipients) {
            personalization.addTo(new Email(recipient));
        }
        mail.addPersonalization(personalization);

        addAttachments(mail, attachmentDTOS);

        Response response = sendMail(mail);
        if (response.getStatusCode() >= 400) {
            log.error("SendGrid error: Status={}, Body={}", response.getStatusCode(), response.getBody());
            throw new IOException("SendGrid API error: " + response.getStatusCode());
        }

        log.info("HTML email sent successfully. Status: {}", response.getStatusCode());
    }

    @Override
    public void sendMailToSuperAdmins(String subject, String text) {
        try {
            String[] validRecipients = sanitizeRecipients(recipients);
            if (validRecipients.length == 0) {
                log.warn("Skip sending mail to super admins because recipients config is empty.");
                return;
            }
            sendHtmlMessage(validRecipients, subject, text, null);
        } catch (IOException e) {
            log.error("Error sending email to super admins", e);
            throw new CustomException(
                    "Failed to send email to super admins",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private void addAttachments(Mail mail, List<EmailAttachmentDTO> attachmentDTOS) {
        if (attachmentDTOS == null || attachmentDTOS.isEmpty()) {
            return;
        }

        for (EmailAttachmentDTO dto : attachmentDTOS) {
            if (dto == null || dto.isEmpty()) {
                continue;
            }

            Attachments attachment = new Attachments();
            attachment.setContent(Base64.getEncoder().encodeToString(dto.getData()));
            attachment.setType(defaultIfBlank(dto.getContentType(), "application/octet-stream"));
            attachment.setFilename(defaultIfBlank(dto.getFileName(), "attachment"));

            if (dto.isInline()) {
                attachment.setDisposition("inline");
                if (dto.getContentId() != null && !dto.getContentId().isBlank()) {
                    attachment.setContentId(dto.getContentId());
                }
            } else {
                attachment.setDisposition("attachment");
            }

            mail.addAttachments(attachment);
        }
    }

    private Response sendMail(Mail mail) throws IOException {
        SendGrid sendGrid = buildClient();
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        return sendGrid.api(request);
    }

    private SendGrid buildClient() {
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            throw new CustomException("SendGrid API key is missing", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new SendGrid(sendGridApiKey);
    }

    private void ensureSuccess(Response response, String message) {
        if (response == null || response.getStatusCode() >= 400) {
            int status = response == null ? 500 : response.getStatusCode();
            String body = response == null ? null : response.getBody();
            log.error("SendGrid error: status={}, body={}", status, body);
            throw new CustomException(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean shouldSkipSendingEmail() {
        return Boolean.FALSE.equals(enableEmails) || MailService.skipMail.get();
    }

    private String[] sanitizeRecipients(String[] to) {
        if (to == null || to.length == 0) {
            return new String[0];
        }

        return Arrays.stream(to)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toArray(String[]::new);
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}