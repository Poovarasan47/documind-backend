package com.documind.service;

import com.documind.config.FileStorageProperties;
import com.documind.dto.DocumentResponse;
import com.documind.model.Document;
import com.documind.model.User;
import com.documind.repository.DocumentRepository;
import com.documind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageProperties fileStorageProperties;
    private final GeminiService geminiService;
    
    private Path fileStorageLocation;
    
    // Initialize storage location
    public void init() {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory!");
        }
    }
    
    public Document uploadDocument(MultipartFile file, String userEmail, String category) {
        // Initialize if not done
        if (fileStorageLocation == null) {
            init();
        }
        
        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }
        
        // Clean filename
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        try {
            // Copy file to upload directory
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Get user
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Extract text and generate AI summary
            String documentText = "";
            String aiSummary = "";
            String aiCategory = category;
            
            try {
                File savedFile = targetLocation.toFile();
                documentText = geminiService.extractText(savedFile, file.getContentType());
                
                // Generate AI summary
                if (!documentText.isEmpty()) {
                    aiSummary = geminiService.summarizeDocument(documentText);
                    
                    // Auto-classify if no category provided
                    if (category == null || category.trim().isEmpty()) {
                        aiCategory = geminiService.classifyDocument(documentText);
                    }
                }
            } catch (Exception e) {
                System.out.println("AI processing failed: " + e.getMessage());
                aiSummary = "AI processing not available";
                aiCategory = category != null ? category : "Uncategorized";
            }
            
            // Create document entity
            Document document = new Document();
            document.setFileName(originalFileName);
            document.setFilePath(fileName);
            document.setFileType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setCategory(aiCategory);
            document.setAiSummary(aiSummary);
            document.setUser(user);
            document.setUploadedAt(LocalDateTime.now());
            
            return documentRepository.save(document);
            
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName);
        }
    }
       
        
        
    
    
    public List<DocumentResponse> getUserDocuments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Document> documents = documentRepository.findByUserIdOrderByUploadedAtDesc(user.getId());
        
        return documents.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public Resource loadFileAsResource(Long documentId, String userEmail) {
        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));
            
            // Security check - ensure document belongs to user
            if (!document.getUser().getEmail().equals(userEmail)) {
                throw new RuntimeException("Access denied");
            }
            
            if (fileStorageLocation == null) {
                init();
            }
            
            Path filePath = this.fileStorageLocation.resolve(document.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found");
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found");
        }
    }
    
    public void deleteDocument(Long documentId, String userEmail) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        // Security check
        if (!document.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied");
        }
        
        try {
            // Delete file from disk
            if (fileStorageLocation == null) {
                init();
            }
            Path filePath = this.fileStorageLocation.resolve(document.getFilePath()).normalize();
            Files.deleteIfExists(filePath);
            
            // Delete from database
            documentRepository.delete(document);
            
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file");
        }
    }
    
    public Document getDocument(Long documentId, String userEmail) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        if (!document.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied");
        }
        
        return document;
    }
    
    private DocumentResponse convertToResponse(Document doc) {
        DocumentResponse response = new DocumentResponse();
        response.setId(doc.getId());
        response.setFileName(doc.getFileName());
        response.setFileType(doc.getFileType());
        response.setFileSize(doc.getFileSize());
        response.setCategory(doc.getCategory());
        response.setAiSummary(doc.getAiSummary());
        response.setUploadedAt(doc.getUploadedAt());
        response.setDownloadUrl("/api/documents/download/" + doc.getId());
        return response;
    }
}