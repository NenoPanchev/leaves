package com.example.leaves.service.impl;

import com.example.leaves.model.entity.RequestEntity;
import com.example.leaves.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Collection;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    public static final String LEAVE_NOTIFY_STRING = "You have %d days paid leave left!\n If you have more than " +
            "5 days left by the end of the year you will lose them!";
    @Value("${ui.address:http://localhost:3000/}")
    public String uiAddress;
    public static final String NEW_REQUEST_NOTIFICATION_MSG = "Employee with name: %s and email: %s" +
            " has requested %s!\n" +
            "Start date: %s\n" +
            "End date: %s\n";
    public static final String CHANGE_PASSWORD_TOKEN_MSG = "You have requested a password change.\nYour token is: %s";
    public static final String CHANGE_PASSWORD_TOKEN_SUBJECT = "Password Change";
    public static final String DEFAULT_FROM = "vacation@lightsoftbulgaria.com";
    @Value("${should-send-emails:false}")
    public boolean shouldSendEmails;
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender, TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.emailSender = emailSender;
    }


    @Override
    public void sendMailToNotifyAboutPaidLeave(
            final String recipientName, final String recipientEmail, String subject, int paidLeaveLeft) throws MessagingException {
        if (!shouldSendEmails) {
            throw new MessagingException();
        }
        // Prepare the evaluation context
        final Context ctx = prepareContext(recipientName, String.format(LEAVE_NOTIFY_STRING, paidLeaveLeft));

        final MimeMessage mimeMessage = prepareMimeMsg(recipientEmail, subject, ctx);

        // Send mail
        this.emailSender.send(mimeMessage);

    }

    @Override
    public void sendMailToNotifyAboutNewRequest(String recipientName,
                                                String recipientEmail,
                                                String subject,
                                                RequestEntity request,
                                                String requestTypeName) throws MessagingException {
        if (!shouldSendEmails) {
            throw new MessagingException();
        }
        String employeeName = request.getEmployee().getUserInfo().getName();
        String employeeEmail = request.getEmployee().getUserInfo().getEmail();

        final Context ctx = prepareContext(recipientName, String.format(NEW_REQUEST_NOTIFICATION_MSG, employeeName, employeeEmail, requestTypeName, request.getStartDate().toString(), request.getEndDate()));

        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = prepareMimeMsg(recipientEmail, subject, ctx);

        // Send mail
        this.emailSender.send(mimeMessage);
        LOGGER.info("Mail sent to {} regarding {} request by {}", recipientName, requestTypeName, employeeName);
    }

    @Override
    public void sendChangePasswordToken(String recipientName,
                                        String recipientEmail,
                                        String token) throws MessagingException {
        if (!shouldSendEmails) {
            throw new MessagingException();
        }
        final Context ctx = prepareContext(recipientName,
                String.format(CHANGE_PASSWORD_TOKEN_MSG,
                        token));

        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = prepareMimeMsg(recipientEmail, CHANGE_PASSWORD_TOKEN_SUBJECT, ctx);

        // Send mail
        this.emailSender.send(mimeMessage);
    }

    @Override
    public void send(final Collection<String> recipients, final String subject, final String text) {
        if (!shouldSendEmails) {
            return;
        }
        try {
            MimeMessage mimeMessage = prepareMimeMsg(recipients, subject, text);
            this.emailSender.send(mimeMessage);
        } catch (Exception e) {
            LOGGER.warn("Error sending mail with subject: {}", subject);
        }
    }

    private Context prepareContext(String recipientName, String content) {
        final Context ctx = new Context();
        ctx.setVariable("name", String.format("Hello %s,", recipientName));
        ctx.setVariable("content", content);
        ctx.setVariable("linkToAction", uiAddress);
        return ctx;
    }

    private MimeMessage prepareMimeMsg(String recipientEmail, String subject, Context ctx) throws MessagingException {
        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.emailSender.createMimeMessage();
        final MimeMessageHelper message =
                new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
        message.setSubject(subject);
        message.setTo(recipientEmail);
        message.setFrom(DEFAULT_FROM);

        // Create the HTML body using Thymeleaf
        final String htmlContent = this.templateEngine.process("DaysLeaveTemplate.html",
                ctx);
        message.setText(htmlContent, true); // true = isHtml
        return mimeMessage;
    }

    private MimeMessage prepareMimeMsg(final Collection<String> recipients, final String subject, final String text) throws MessagingException {
        final MimeMessage message = this.emailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(recipients.toArray(new String[]{}));
        helper.setSubject(subject);
        helper.setText(text, false);
        helper.setFrom(DEFAULT_FROM);
        return message;
    }
}
