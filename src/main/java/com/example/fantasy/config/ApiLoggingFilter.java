package com.example.fantasy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingFilter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Only log API endpoints
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(formatter);
        
        // Wrap request and response to cache content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // Log incoming request
            logRequest(wrappedRequest, timestamp);
            
            // Process the request
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
            // Log outgoing response
            long duration = System.currentTimeMillis() - startTime;
            logResponse(wrappedRequest, wrappedResponse, timestamp, duration);
            
        } finally {
            // Important: copy body to response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String timestamp) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;
        
        // Get headers (excluding sensitive ones)
        String headers = Collections.list(request.getHeaderNames())
                .stream()
                .filter(headerName -> !isSensitiveHeader(headerName))
                .map(headerName -> headerName + "=" + request.getHeader(headerName))
                .collect(Collectors.joining(", "));
        
        log.info("[API][Request][{}] {} {} | Headers: {}", 
                timestamp, method, fullUrl, headers);
        
        // Log request body for POST/PUT/PATCH requests
        if (hasBody(method)) {
            String body = getRequestBody(request);
            if (body != null && !body.isEmpty()) {
                String maskedBody = maskSensitiveData(body);
                log.info("[API][Request][{}] Body: {}", timestamp, maskedBody);
            }
        }
    }

    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, 
                           String timestamp, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        
        log.info("[API][Response][{}] {} {} | Status: {} | Duration: {}ms", 
                timestamp, method, uri, status, duration);
        
        // Log response body for non-2xx status codes or if it's small
        String responseBody = getResponseBody(response);
        if (responseBody != null && !responseBody.isEmpty()) {
            if (status >= 400 || responseBody.length() < 1000) {
                String maskedBody = maskSensitiveData(responseBody);
                log.info("[API][Response][{}] Body: {}", timestamp, maskedBody);
            } else {
                log.info("[API][Response][{}] Body: [Response body too large - {} chars]", 
                        timestamp, responseBody.length());
            }
        }
    }

    private boolean hasBody(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") || 
               lowerName.contains("cookie") || 
               lowerName.contains("token");
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, request.getCharacterEncoding());
            }
        } catch (Exception e) {
            log.warn("Could not read request body: {}", e.getMessage());
        }
        return null;
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        try {
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, response.getCharacterEncoding());
            }
        } catch (Exception e) {
            log.warn("Could not read response body: {}", e.getMessage());
        }
        return null;
    }

    private String maskSensitiveData(String body) {
        if (body == null) return null;
        
        // Mask password fields
        String masked = body.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
        
        // Mask token fields
        masked = masked.replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***\"");
        
        // Mask any field containing 'secret' or 'key'
        masked = masked.replaceAll("\"[^\"]*(?:secret|key)[^\"]*\"\\s*:\\s*\"[^\"]*\"", "\"***\":\"***\"");
        
        return masked;
    }
}