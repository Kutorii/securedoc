package com.example.securedoc.controller;

import com.example.securedoc.domain.Response;
import com.example.securedoc.service.impl.DocumentStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Arrays;

import static com.example.securedoc.utils.DocumentUtils.*;
import static com.example.securedoc.utils.RequestUtils.getResponse;
import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/document")
public class StorageController {
    private final DocumentStorageService storageService;
    private final String[] ALLOWED_EXT = {"doc, pdf, xls"};

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('document:create')")
    public ResponseEntity<Response> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (!isFileExtensionAllowed(file.getOriginalFilename())) {
            throw new IllegalArgumentException("File extension is not supported");
        }
        storageService.store(file);

        return ResponseEntity.created(getUri()).body(getResponse(request, emptyMap(), "File uploaded", OK));
    }

    @GetMapping("/{filename.+}")
    @PreAuthorize("hasAuthority('document:read')")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        var file = storageService.loadAsResource(filename);

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"").body(file);
    }

    private boolean isFileExtensionAllowed(String fileName) {
        var extension = getFileExtension(fileName);

        return Arrays.asList(ALLOWED_EXT).contains(extension);
    }

    private URI getUri() {
        return URI.create("");
    }
}
