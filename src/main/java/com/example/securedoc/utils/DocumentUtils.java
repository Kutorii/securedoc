package com.example.securedoc.utils;

import com.example.securedoc.dto.Document;
import com.example.securedoc.dto.User;
import com.example.securedoc.entity.DocumentEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static com.example.securedoc.constant.Constants.*;

public class DocumentUtils {
    //TODO add icon depending on the file extension
    public static DocumentEntity createDocumentEntity(String fileName, String fileExtension, Long sizeInBytes, String formattedSize) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var referenceId = user.getUserId();

        return DocumentEntity.builder()
                .documentId(UUID.randomUUID().toString())
                .referenceId(referenceId)
                .extension(fileExtension)
                .formattedSize(formattedSize)
                .icon("")
                .name(fileName)
                .size(sizeInBytes)
                .uri(DOCUMENT_ROOT_LOCATION + fileName)
                .build();
    }

    public static Document fromEntity(DocumentEntity documentEntity) {
        var document = new Document();
        BeanUtils.copyProperties(documentEntity, document);

        return document;
    }

    public static String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            if (StringUtils.countMatches(fileName, ".") > 1) {
                throw new IllegalArgumentException("Filename contains multiple extensions");
            }

            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }

        return "Unknown";
    }
}
