package com.example.securedoc.controller;

import com.example.securedoc.domain.Response;
import com.example.securedoc.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.example.securedoc.utils.RequestUtils.getResponse;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/document")
public class DocumentController {
    private final DocumentService documentService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('document:read')")
    public ResponseEntity<Response> documents(HttpServletRequest request) {
        var documents = documentService.findAll();

        return ResponseEntity.ok().body(getResponse(request, Map.of("document", documents), "Fetching all documents successful", OK));
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasAuthority('document:read')")
    public ResponseEntity<Response> document(@PathVariable String documentId, HttpServletRequest request) {
        var document = documentService.getDocumentById(documentId);

        return ResponseEntity.ok().body(getResponse(request, Map.of("document", document), "Fetching document successful", OK));
    }
}
