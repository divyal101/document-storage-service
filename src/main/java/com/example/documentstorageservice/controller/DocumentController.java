package com.example.documentstorageservice.controller;

import com.example.documentstorageservice.model.Document;
import com.example.documentstorageservice.repository.DocumentRepository;
import com.example.documentstorageservice.service.EncryptionService;
import com.example.documentstorageservice.service.NotificationService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final EncryptionService encryptionService;
    private final NotificationService notificationService;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Autowired
    public DocumentController(DocumentRepository documentRepository, EncryptionService encryptionService, NotificationService notificationService) {
        this.documentRepository = documentRepository;
        this.encryptionService = encryptionService;
        this.notificationService = notificationService;
    }

    private Bucket getBucket() {
        String ipAddress = getClientIP();
        return buckets.computeIfAbsent(ipAddress, k -> newBucket());
    }

    private Bucket newBucket() {
        Refill refill = Refill.greedy(10, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(10, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIP() {
        // Logic to retrieve client IP address...
        return "client-ip";
    }

    @PostMapping
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam("userEmail") String userEmail) throws Exception {
        Bucket bucket = getBucket();
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            Document document = new Document();
            document.setName(file.getOriginalFilename());
            String encryptedContent = encryptionService.encrypt(file.getBytes());
            document.setEncryptedContent(encryptedContent);
            Document savedDocument = documentRepository.save(document);

            notificationService.sendNotification("Document uploaded successfully", userEmail);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("X-Rate-Limit-Retry-After-Milliseconds", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000)).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long id) throws Exception {
        Optional<Document> documentOpt = documentRepository.findById(id);
        if (documentOpt.isPresent()) {
            Document document = documentOpt.get();
            byte[] decryptedContent = encryptionService.decrypt(document.getEncryptedContent());
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + document.getName() + "\"")
                    .body(decryptedContent);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public Page<Document> getAllDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return documentRepository.findAll(PageRequest.of(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam String query) {
        List<Document> documents = documentRepository.findByNameContaining(query);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

