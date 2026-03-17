package com.ra.base_spring_boot.security.oauth2;

import com.ra.base_spring_boot.dto.resp.OAuth2Response;
import com.ra.base_spring_boot.service.auth.OAuth2Service;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2Service oAuth2Service;

    @Value("${app.frontend.url:}")
    private String frontendUrl;

    @Value("${app.frontend.oauth2-redirect-path:/oauth2/redirect}")
    private String oauth2RedirectPath;

    @PostConstruct
    public void init() {
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        log.info("=== OAuth2SuccessHandler.onAuthenticationSuccess called ===");
        log.info("Request URI: {}", request.getRequestURI());

        try {
            if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
                log.error("Invalid authentication type: {}",
                        authentication != null ? authentication.getClass() : "null");
                redirectToErrorPage(request, response, "Invalid OAuth2 authentication type");
                return;
            }

            String providerId = oauth2Token.getAuthorizedClientRegistrationId();
            log.info("Processing OAuth2 login for provider: {}", providerId);

            OAuth2Response auth = oAuth2Service.processOAuth2Login(oauth2Token);

            if (auth == null) {
                log.error("OAuth2Response is null after processing login");
                redirectToErrorPage(request, response, "Không thể xử lý thông tin đăng nhập");
                return;
            }

            log.info("OAuth2Response received - Username: {}, Email: {}, Provider: {}",
                    auth.getUsername(), auth.getEmail(), auth.getProvider());

            if (auth.getToken() != null && !auth.getToken().isBlank()) {
                Cookie cookie = new Cookie("ACCESS_TOKEN", auth.getToken());
                cookie.setHttpOnly(true);
                cookie.setSecure(request.isSecure());
                cookie.setPath("/");
                cookie.setMaxAge(24 * 60 * 60);
                response.addCookie(cookie);
                log.info("ACCESS_TOKEN cookie set (HttpOnly).");
            } else {
                log.warn("No token generated; cookie not set.");
            }

            String base = (frontendUrl != null && !frontendUrl.isBlank())
                    ? frontendUrl + oauth2RedirectPath
                    : "http://localhost:8080/oauth2/test-result";

            StringBuilder redirectUrlBuilder = new StringBuilder(base);
            redirectUrlBuilder.append("?");

            redirectUrlBuilder.append("provider=").append(auth.getProvider() != null ? auth.getProvider().name() : "");
            redirectUrlBuilder.append("&isNewUser=").append(auth.isNewUser());
            redirectUrlBuilder.append("&tokenType=")
                    .append(auth.getTokenType() != null ? auth.getTokenType() : "Bearer");

            if (auth.getToken() != null && !auth.getToken().isBlank()) {
                redirectUrlBuilder.append("&token=").append(URLEncoder.encode(auth.getToken(), StandardCharsets.UTF_8));
            }
            if (auth.getUsername() != null) {
                redirectUrlBuilder.append("&username=")
                        .append(URLEncoder.encode(auth.getUsername(), StandardCharsets.UTF_8));
            }
            if (auth.getEmail() != null) {
                redirectUrlBuilder.append("&email=").append(URLEncoder.encode(auth.getEmail(), StandardCharsets.UTF_8));
            }
            if (auth.getFullName() != null) {
                redirectUrlBuilder.append("&fullName=")
                        .append(URLEncoder.encode(auth.getFullName(), StandardCharsets.UTF_8));
            }
            if (auth.getAvatar() != null && auth.getAvatar().getFileUrl() != null) {
                redirectUrlBuilder.append("&avatar=")
                        .append(URLEncoder.encode(auth.getAvatar().getFileUrl(), StandardCharsets.UTF_8));
            }
            if (auth.getMessage() != null) {
                redirectUrlBuilder.append("&message=")
                        .append(URLEncoder.encode(auth.getMessage(), StandardCharsets.UTF_8));
            }

            String redirectUrl = redirectUrlBuilder.toString();

            log.info("OAuth2 login successful - Redirecting to: {}", redirectUrl);
            log.info("User: {} ({}) via {}", auth.getUsername(), auth.getEmail(), auth.getProvider());
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("ERROR in OAuth2SuccessHandler: ", e);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
            redirectToErrorPage(request, response,
                    "Lỗi xử lý đăng nhập: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    private void redirectToErrorPage(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws IOException {
        String base = (frontendUrl != null && !frontendUrl.isBlank())
                ? frontendUrl + oauth2RedirectPath
                : "http://localhost:8080/oauth2/test-result";

        String redirectUrl = UriComponentsBuilder.fromUriString(base)
                .queryParam("error", java.net.URLEncoder.encode(errorMessage, java.nio.charset.StandardCharsets.UTF_8))
                .build().toUriString();

        log.error("Redirecting to error page: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
