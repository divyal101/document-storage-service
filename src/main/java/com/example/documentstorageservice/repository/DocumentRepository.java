package com.example.documentstorageservice.repository;

import com.example.documentstorageservice.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByNameContaining(String query);
}
