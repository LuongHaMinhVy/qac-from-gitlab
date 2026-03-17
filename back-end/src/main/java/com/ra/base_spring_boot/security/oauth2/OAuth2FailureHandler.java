package com.ra.base_spring_boot.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Component
@Slf4j
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend.url:}")
    private String frontendUrl;

    @Value("${app.frontend.oauth2-redirect-path:/oauth2/redirect}")
    private String oauth2RedirectPath;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 authentication failed: {}", exception.getMessage(), exception);

        String errorMessage = "Đăng nhập thất bại. Vui lòng thử lại.";


        if (exception.getMessage() != null) {
            if (exception.getMessage().contains("access_denied")) {
                errorMessage = "Bạn đã hủy đăng nhập";
            } else if (exception.getMessage().contains("invalid_token")) {
                errorMessage = "Token không hợp lệ";
            } else if (exception.getMessage().contains("email")) {
                errorMessage = "Không thể lấy email từ tài khoản. Vui lòng cấp quyền truy cập email.";
            }
        }


        String redirectBaseUrl;
        if (frontendUrl != null && !frontendUrl.isEmpty() &&
                !frontendUrl.equals("http://localhost:5173") &&
                !frontendUrl.equals("localhost:5173") &&
                !frontendUrl.trim().isEmpty()) {

            redirectBaseUrl = frontendUrl + oauth2RedirectPath;
            log.info("Using frontend URL for error: {}", redirectBaseUrl);
        } else {

            redirectBaseUrl = "http://localhost:8080/oauth2/test-result";
            log.info("No frontend configured (frontendUrl='{}'), using test page for error: {}",
                    frontendUrl, redirectBaseUrl);
        }

        String redirectUrl = UriComponentsBuilder
                .fromUriString(redirectBaseUrl)
                .queryParam("error", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                .build()
                .toUriString();

        log.info("Redirecting to frontend with error: {}", redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
