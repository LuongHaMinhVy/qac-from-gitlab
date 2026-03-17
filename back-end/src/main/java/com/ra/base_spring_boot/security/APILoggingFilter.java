package com.ra.base_spring_boot.security;

import com.ra.base_spring_boot.model.ApiLog;
import com.ra.base_spring_boot.repository.ApiLogRepository;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class APILoggingFilter extends OncePerRequestFilter {

    private final ApiLogRepository apiLogRepository;
    private final IAccountRepository accountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().contains("/api/v1")) {
            long startTime = System.currentTimeMillis();

            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

            try {
                filterChain.doFilter(requestWrapper, responseWrapper);
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                saveLog(requestWrapper, responseWrapper, duration);
                responseWrapper.copyBodyToResponse();
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void saveLog(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        try {
            String method = request.getMethod();
            String endpoint = request.getRequestURI();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            int statusCode = response.getStatus();

            String headers = Collections.list(request.getHeaderNames()).stream()
                    .map(h -> h + ": " + request.getHeader(h))
                    .collect(Collectors.joining("\n"));

            String requestBody = new String(request.getContentAsByteArray());
            String responseBody = new String(response.getContentAsByteArray());

            if (requestBody.contains("\"password\"")) {
                requestBody = requestBody.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"****\"");
            }

            ApiLog apiLog = ApiLog.builder()
                    .method(method)
                    .endpoint(endpoint)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .statusCode(statusCode)
                    .responseTime((int) duration)
                    .requestHeaders(headers)
                    .requestBody(requestBody)
                    .responseBody(responseBody)
                    .createdAt(LocalDateTime.now())
                    .build();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                accountRepository.findByUsername(auth.getName()).ifPresent(apiLog::setAccount);
            }

            apiLogRepository.save(apiLog);
        } catch (Exception e) {
            log.error("Error saving API log: ", e);
        }
    }
}
