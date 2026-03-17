package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.SocialAccountResponse;
import com.ra.base_spring_boot.model.constants.SocialProvider;
import com.ra.base_spring_boot.service.auth.OAuth2Service;
import com.ra.base_spring_boot.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2", description = "Quản lý OAuth2")
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @GetMapping("/social-accounts")
    public ResponseEntity<ApiResponse<List<SocialAccountResponse>>> getSocialAccounts() {

        try {

            Integer accountId = SecurityUtils.getCurrentAccountId();

            if (accountId == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.fail("Unauthorized", null));
            }

            List<SocialAccountResponse> socialAccounts = oAuth2Service.getUserSocialAccounts(accountId);

            return ResponseEntity.ok(ApiResponse.success(
                    socialAccounts,
                    "Lấy danh sách social accounts thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.fail("Lỗi: " + e.getMessage(), null));
        }
    }

    @GetMapping("/check-link/{provider}")
    public ResponseEntity<ApiResponse<Boolean>> checkLink(@PathVariable String provider) {

        try {
            Integer accountId = SecurityUtils.getCurrentAccountId();

            if (accountId == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.fail("Unauthorized", null));
            }

            SocialProvider socialProvider;
            try {
                socialProvider = SocialProvider.valueOf(provider.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.fail("Provider không hợp lệ: " + provider, null));
            }

            boolean isLinked = oAuth2Service.isAccountLinked(accountId, socialProvider);

            return ResponseEntity.ok(ApiResponse.success(
                    isLinked,
                    isLinked ? "Đã liên kết" : "Chưa liên kết"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.fail("Lỗi: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/unlink/{provider}")
    public ResponseEntity<ApiResponse<String>> unlinkSocialAccount(@PathVariable String provider) {

        try {
            Integer accountId = SecurityUtils.getCurrentAccountId();

            if (accountId == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.fail("Unauthorized", null));
            }

            SocialProvider socialProvider;
            try {
                socialProvider = SocialProvider.valueOf(provider.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.fail("Provider không hợp lệ: " + provider, null));
            }

            String message = oAuth2Service.unlinkSocialAccount(accountId, socialProvider);

            return ResponseEntity.ok(ApiResponse.success(message, message));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.fail(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.fail("Lỗi: " + e.getMessage(), null));
        }
    }
}
