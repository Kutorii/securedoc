package com.example.securedoc.dto;

import lombok.Data;

@Data
public class Document {
    private String documentId;
    private String referenceId;
    private String extension;
    private String formattedSize;
    private String icon;
    private String name;
    private Long size;
    private String uri;
    private String description;
}
