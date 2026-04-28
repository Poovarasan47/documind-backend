package com.documind.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String apiUrl;
    
    private final Map<String, LocalDateTime> rateLimitCache;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    
    // Rate limit: 1 request per user per 15 seconds
    private static final int RATE_LIMIT_SECONDS = 15;
    
   

    @Cacheable(value = "summaries", key = "#documentText.hashCode()")
    public String summarizeDocument(String documentText, String userEmail) {
        checkRateLimit(userEmail);
        
        if (documentText == null || documentText.trim().isEmpty()) {
            return "No content to summarize";
        }
        
        String limitedText = documentText.length() > 3000 
                ? documentText.substring(0, 3000) + "..." 
                : documentText;
                
        String prompt = "Summarize the following document in 3-5 concise bullet points:\n\n" + limitedText;
        return callGemini(prompt);
    }
    
    @Cacheable(value = "answers", key = "#documentText.hashCode() + '-' + #question")
    public String answerQuestion(String documentText, String question, String userEmail) {
        checkRateLimit(userEmail);
        
        if (documentText == null || documentText.trim().isEmpty()) {
            return "No document content available to answer questions";
        }
        
        String limitedText = documentText.length() > 2500 
                ? documentText.substring(0, 2500) + "..." 
                : documentText;
                
        String prompt = String.format(
            "Based on this document content:\n%s\n\nAnswer this question: %s\n\nIf the answer is not in the document, say 'This information is not found in the document.'",
            limitedText, question
        );
        
        return callGemini(prompt);
    }
    
    @Cacheable(value = "classifications", key = "#documentText.hashCode()")
    public String classifyDocument(String documentText, String userEmail) {
        checkRateLimit(userEmail);
        
        if (documentText == null || documentText.trim().isEmpty()) {
            return "Uncategorized";
        }
        
        String limitedText = documentText.length() > 1500 
                ? documentText.substring(0, 1500) 
                : documentText;
                
        String prompt = "Classify this document into ONE of these categories ONLY: Invoice, Report, Contract, Email, Letter, Resume, Article, Other.\n\n" +
                "Document content:\n" + limitedText + "\n\n" +
                "Respond with ONLY the category name, nothing else.";
                
        return callGemini(prompt).trim();
    }
    
    @Cacheable(value = "keypoints", key = "#documentText.hashCode()")
    public String extractKeyPoints(String documentText, String userEmail) {
        checkRateLimit(userEmail);
        
        if (documentText == null || documentText.trim().isEmpty()) {
            return "No content to extract key points";
        }
        
        String limitedText = documentText.length() > 3000 
                ? documentText.substring(0, 3000) + "..." 
                : documentText;
                
        String prompt = "Extract the 5 most important key points from this document:\n\n" + limitedText;
        return callGemini(prompt);
    }

   
    
    private void checkRateLimit(String userEmail) {
        LocalDateTime lastRequest = rateLimitCache.get(userEmail);
        LocalDateTime now = LocalDateTime.now();
        
        if (lastRequest != null) {
            long secondsSinceLastRequest = ChronoUnit.SECONDS.between(lastRequest, now);
            if (secondsSinceLastRequest < RATE_LIMIT_SECONDS) {
                long waitTime = RATE_LIMIT_SECONDS - secondsSinceLastRequest;
                throw new RuntimeException("Rate limit exceeded. Please wait " + waitTime + " seconds.");
            }
        }
        rateLimitCache.put(userEmail, now);
    }
    
    private String callGemini(String prompt) {
        try {
            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", prompt);
            
            JsonArray parts = new JsonArray();
            parts.add(textPart);
            
            JsonObject content = new JsonObject();
            content.add("parts", parts);
            
            JsonArray contents = new JsonArray();
            contents.add(content);
            
            JsonObject requestBody = new JsonObject();
            requestBody.add("contents", contents);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            
            if (jsonResponse.has("candidates")) {
                return jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
            } else {
    System.out.println("🚨 GOOGLE API ERROR: " + response.body());
    return "AI is currently experiencing high global demand. Please try asking a specific question in the chat below!";
}
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    
    
    public String extractText(File file, String fileType) throws IOException {
        if (fileType.contains("pdf")) {
            return extractTextFromPDF(file);
        } else if (fileType.contains("word") || fileType.contains("document")) {
            return extractTextFromDOCX(file);
        } else if (fileType.contains("text")) {
            return extractTextFromTXT(file);
        } else {
            return "Unsupported file type for text extraction";
        }
    }
    
    private String extractTextFromPDF(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) { 
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    private String extractTextFromDOCX(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            StringBuilder text = new StringBuilder();
            
            for (XWPFParagraph para : paragraphs) {
                text.append(para.getText()).append("\n");
            }
            return text.toString();
        }
    }
    
    private String extractTextFromTXT(File file) throws IOException {
        return Files.readString(Path.of(file.getPath()));
    }
}
