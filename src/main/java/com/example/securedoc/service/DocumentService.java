package com.example.securedoc.service;

import com.example.securedoc.dto.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

public interface DocumentService {

    Collection<Document> findAll();

    void createDocument(MultipartFile file);

    Document getDocumentById(String documentId);
}
