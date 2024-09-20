package com.example.securedoc.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Entity
@Table(name = "documents")
public class DocumentEntity extends Auditable {
    @Column(name = "document_id", unique = true, nullable = false)
    private String documentId;

    @Column(name = "reference_id", nullable = false)
    private String referenceId;

    @Column(nullable = false)
    private String extension;

    @Column(name = "formatted_size", nullable = false)
    private String formattedSize;

    @Column(nullable = false)
    private String icon;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String uri;

    private String description;
}
