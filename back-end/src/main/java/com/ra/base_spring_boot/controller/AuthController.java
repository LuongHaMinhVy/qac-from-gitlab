package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.*;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.LoginResponse;
import com.ra.base_spring_boot.service.auth.AccountActivationService;
import com.ra.base_spring_boot.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AccountActivationService  accountActivationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody FormLogin request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(authService.logout(authorizationHeader));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody FormRegister req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activate(@RequestBody ActivateAccountRequest req) {
        return ResponseEntity.ok(authService.activateAccount(req));
    }

    @PostMapping("/resend-activation")
    @Operation(summary = "Resent active code")
    public ResponseEntity<ApiResponse<Object>> resendActivation(@RequestParam String email) {
        accountActivationService.resendActivationToken(email);
        return ResponseEntity.ok(ApiResponse.success(null, "New activation code sent to your email!"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(authService.refreshToken(req));
    }

    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        return authService.changePassword(req);
    }
    @Operation(summary = "Reset password")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request.getToken(), request.getNewPassword()));
    }

    @Operation(summary = "Forgot password")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(authService.sendResetPasswordToken(email));
    }
}
