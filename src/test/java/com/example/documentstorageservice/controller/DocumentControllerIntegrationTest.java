package com.example.documentstorageservice.controller;

import com.example.documentstorageservice.model.Document;
import com.example.documentstorageservice.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class DocumentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    public void setup() {
        documentRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    public void shouldUploadAndRetrieveDocument() throws Exception {
        // Upload a document
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test content".getBytes()
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documents")
                        .file(file)
                        .param("userEmail", "user@example.com"))  // Add userEmail parameter here
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(print())
                .andReturn();

        Document uploadedDocument = documentRepository.findAll().get(0);
        assertThat(uploadedDocument).isNotNull();
        assertThat(uploadedDocument.getName()).isEqualTo("test.txt");

        // Retrieve the document
        mockMvc.perform(MockMvcRequestBuilders.get("/api/documents/" + uploadedDocument.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Content-Disposition", "attachment; filename=\"test.txt\""))
                .andExpect(MockMvcResultMatchers.content().string("Test content"))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    public void shouldListAllDocuments() throws Exception {
        Document doc1 = new Document();
        doc1.setName("doc1.txt");
        doc1.setEncryptedContent("content1");
        documentRepository.save(doc1);

        Document doc2 = new Document();
        doc2.setName("doc2.txt");
        doc2.setEncryptedContent("content2");
        documentRepository.save(doc2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/documents"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    public void shouldDeleteDocument() throws Exception {
        Document document = new Document();
        document.setName("test.txt");
        document.setEncryptedContent("content");
        Document savedDocument = documentRepository.save(document);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/documents/" + savedDocument.getId()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        assertThat(documentRepository.findById(savedDocument.getId())).isEmpty();
    }
}


