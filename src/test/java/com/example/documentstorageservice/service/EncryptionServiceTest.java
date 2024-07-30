package com.example.documentstorageservice.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EncryptionServiceTest {

    private final EncryptionService encryptionService = new EncryptionService();

    @Test
    public void testEncryptionDecryption() throws Exception {
        String originalContent = "Test content";
        String encryptedContent = encryptionService.encrypt(originalContent.getBytes());
        byte[] decryptedContent = encryptionService.decrypt(encryptedContent);
        assertEquals(originalContent, new String(decryptedContent));
    }
}
