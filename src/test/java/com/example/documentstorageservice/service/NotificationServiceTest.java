package com.example.documentstorageservice.service;

import org.junit.jupiter.api.Test;

public class NotificationServiceTest {

    private final NotificationService notificationService = new NotificationService();

    @Test
    public void testSendNotification() {
        String message = "Test message";
        String email = "test@example.com";
        notificationService.sendNotification(message, email);
        // Add assertions if needed to verify notification logic
    }
}
