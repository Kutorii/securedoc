package com.example.securedoc.service.impl;

import com.example.securedoc.dto.Document;
import com.example.securedoc.entity.DocumentEntity;
import com.example.securedoc.exception.dto.DocumentNotFoundException;
import com.example.securedoc.repository.DocumentRepository;
import com.example.securedoc.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;

import static com.example.securedoc.utils.DocumentUtils.*;
import static com.example.securedoc.utils.DocumentUtils.createDocumentEntity;
import static com.example.securedoc.utils.DocumentUtils.getFileExtension;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;

    @Override
    public Collection<Document> findAll() {
        var documentEntities = documentRepository.findAll();

        var documents = new ArrayList<Document>();
        for (DocumentEntity de : documentEntities) {
            documents.add(fromEntity(de));
        }

        return documents;
    }

    @Override
    public void createDocument(MultipartFile file) {
        var originalFileName = sanitizeFilename(file.getOriginalFilename());
        var fileExtension = getFileExtension(originalFileName);
        var sizeInBytes = file.getSize();
        var formattedSize = formatFileSize(sizeInBytes);

        documentRepository.save(createDocumentEntity(originalFileName, fileExtension, sizeInBytes, formattedSize));
    }

    private String formatFileSize(long sizeInBytes) {
        var units = new String[] { "B", "KB", "MB", "GB", "TB" };
        var unitIndex = 0;
        var size = (double) sizeInBytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    @Override
    public Document getDocumentById(String documentId) {
        var documentEntity = documentRepository.findByDocumentId(documentId).orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        return fromEntity(documentEntity);
    }
}
