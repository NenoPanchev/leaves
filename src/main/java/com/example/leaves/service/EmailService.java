package com.example.leaves.service;

import com.example.leaves.model.entity.RequestEntity;

import javax.mail.MessagingException;

public interface EmailService {


    void sendMailToNotifyAboutPaidLeave(
            final String recipientName,
            final String recipientEmail,
            final String subject,
            int paidLeaveLeft) throws MessagingException;

    void sendMailToNotifyAboutNewRequest(String recipientName,
                                         String recipientEmail,
                                         String subject,
                                         RequestEntity request) throws MessagingException;

    void sendChangePasswordToken(
            final String recipientName,
            final String recipientEmail,
            String token) throws MessagingException;
}
