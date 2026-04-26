package com.documind.controller;

import com.documind.dto.MessageResponse;
import com.documind.model.Document;
import com.documind.service.DocumentService;
import com.documind.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AIController {
    
    private final GeminiService geminiService;
    private final DocumentService documentService;
    
    @PostMapping("/summarize/{documentId}")
    public ResponseEntity<?> summarizeDocument(
            @PathVariable Long documentId,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            Document document = documentService.getDocument(documentId, userEmail);
            
            // Get file path
            Path filePath = Paths.get("uploads").resolve(document.getFilePath());
            File file = filePath.toFile();
            
            // Extract text and summarize (🚨 Added userEmail)
            String text = geminiService.extractText(file, document.getFileType());
            String summary = geminiService.summarizeDocument(text, userEmail);
            
            Map<String, String> response = new HashMap<>();
            response.put("summary", summary);
            response.put("documentId", documentId.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Summarization failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/ask/{documentId}")
    public ResponseEntity<?> askQuestion(
            @PathVariable Long documentId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            String question = request.get("question");
            
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Question is required"));
            }
            
            Document document = documentService.getDocument(documentId, userEmail);
            
            // Get file path
            Path filePath = Paths.get("uploads").resolve(document.getFilePath());
            File file = filePath.toFile();
            
            // Extract text and answer question (🚨 Added userEmail)
            String text = geminiService.extractText(file, document.getFileType());
            String answer = geminiService.answerQuestion(text, question, userEmail);
            
            Map<String, String> response = new HashMap<>();
            response.put("question", question);
            response.put("answer", answer);
            response.put("documentId", documentId.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Question answering failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/classify/{documentId}")
    public ResponseEntity<?> classifyDocument(
            @PathVariable Long documentId,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            Document document = documentService.getDocument(documentId, userEmail);
            
            // Get file path
            Path filePath = Paths.get("uploads").resolve(document.getFilePath());
            File file = filePath.toFile();
            
            // Extract text and classify (🚨 Added userEmail)
            String text = geminiService.extractText(file, document.getFileType());
            String category = geminiService.classifyDocument(text, userEmail);
            
            Map<String, String> response = new HashMap<>();
            response.put("category", category);
            response.put("documentId", documentId.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Classification failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/key-points/{documentId}")
    public ResponseEntity<?> extractKeyPoints(
            @PathVariable Long documentId,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            Document document = documentService.getDocument(documentId, userEmail);
            
            // Get file path
            Path filePath = Paths.get("uploads").resolve(document.getFilePath());
            File file = filePath.toFile();
            
            // Extract text and get key points (🚨 Added userEmail)
            String text = geminiService.extractText(file, document.getFileType());
            String keyPoints = geminiService.extractKeyPoints(text, userEmail);
            
            Map<String, String> response = new HashMap<>();
            response.put("keyPoints", keyPoints);
            response.put("documentId", documentId.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Key points extraction failed: " + e.getMessage()));
        }
    }
}