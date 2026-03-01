package com.documind.controller;

import com.documind.dto.DocumentResponse;
import com.documind.dto.MessageResponse;
import com.documind.dto.UploadResponse;
import com.documind.model.Document;
import com.documind.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentController {
    
    private final DocumentService documentService;
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            Document document = documentService.uploadDocument(file, userEmail, category);
            
            return ResponseEntity.ok(new UploadResponse(
                    "File uploaded successfully",
                    document.getFileName(),
                    document.getId(),
                    "/api/documents/download/" + document.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Upload failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/my-documents")
    public ResponseEntity<List<DocumentResponse>> getMyDocuments(Authentication authentication) {
        String userEmail = authentication.getName();
        List<DocumentResponse> documents = documentService.getUserDocuments(userEmail);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            Document document = documentService.getDocument(id, userEmail);
            Resource resource = documentService.loadFileAsResource(id, userEmail);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + document.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            documentService.deleteDocument(id, userEmail);
            return ResponseEntity.ok(new MessageResponse("Document deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Delete failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            Document document = documentService.getDocument(id, userEmail);
            
            DocumentResponse response = new DocumentResponse();
            response.setId(document.getId());
            response.setFileName(document.getFileName());
            response.setFileType(document.getFileType());
            response.setFileSize(document.getFileSize());
            response.setCategory(document.getCategory());
            response.setAiSummary(document.getAiSummary());
            response.setUploadedAt(document.getUploadedAt());
            response.setDownloadUrl("/api/documents/download/" + document.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
