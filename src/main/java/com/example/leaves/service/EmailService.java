package com.example.leaves.service;

public interface EmailService {
     void sendMessage(
            String to, String subject, String text);
}
