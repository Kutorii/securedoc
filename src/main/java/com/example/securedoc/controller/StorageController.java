package com.example.securedoc.controller;

import com.example.securedoc.domain.Response;
import com.example.securedoc.service.impl.DocumentStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

import static com.example.securedoc.utils.RequestUtils.getResponse;
import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/document")
public class StorageController {
    private final DocumentStorageService storageService;

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('document:create')")
    public ResponseEntity<Response> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        storageService.store(file);

        return ResponseEntity.created(getUri()).body(getResponse(request, emptyMap(), "File uploaded", OK));
    }

    @GetMapping("/download/{filename:.+}")
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

    private URI getUri() {
        return URI.create("");
    }
}
