package com.example.documentstorageservice.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendNotification(String message, String userEmail) {
        // Logic to send email notification
        System.out.println("Notification sent to " + userEmail + ": " + message);
    }
}
