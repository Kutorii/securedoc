package com.example.securedoc.service.impl;

import com.example.securedoc.exception.storage.StorageException;
import com.example.securedoc.exception.storage.StorageFileNotFoundException;
import com.example.securedoc.service.DocumentService;
import com.example.securedoc.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;

import static com.example.securedoc.constant.Constants.*;

@Service
@RequiredArgsConstructor
public class DocumentStorageService implements StorageService {
    private final DocumentService documentService;
    private final Path rootLocation = Paths.get(DOCUMENT_ROOT_LOCATION);

    @Override
    public void store(MultipartFile file) {
        try {
            if (file == null) {
                throw new StorageException("File is null");
            }

            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file");
            }

            var destinationFile = this.rootLocation.resolve(Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new StorageException("Cannot store file outside of current directory");
            }

            if (Files.exists(destinationFile)) {
                throw new FileAlreadyExistsException("File already exists");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            documentService.createDocument(file);
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    @Override
    public Path load(String fileName) {
        return this.rootLocation.resolve(fileName);
    }

    @Override
    public Resource loadAsResource(String fileName) {
        try {
            var file = load(fileName);
            var resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file : " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file : " + fileName, e);
        }
    }
}
