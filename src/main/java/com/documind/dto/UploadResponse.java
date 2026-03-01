package com.documind.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResponse {
    private String message;
    private String fileName;
    private Long documentId;
    private String downloadUrl;
}