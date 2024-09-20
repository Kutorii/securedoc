package com.example.securedoc.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {

    void store(MultipartFile file);

    Path load(String fileName);

    Resource loadAsResource(String fileName);
}
