package com.ra.base_spring_boot.service.auth;


import com.ra.base_spring_boot.dto.req.*;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.LoginResponse;

import java.util.Map;

public interface AuthService {
    ApiResponse<LoginResponse> login(FormLogin req);
    ApiResponse<String> logout(String token);
    ApiResponse<Map<String, String>> refreshToken(RefreshTokenRequest req);
    ApiResponse<String> register(FormRegister req);
    ApiResponse<String> activateAccount(ActivateAccountRequest req);
    ApiResponse<String> changePassword(ChangePasswordRequest req);
    ApiResponse<Object> sendResetPasswordToken(String email);
    ApiResponse<Object> resetPassword(String token, String newPassword);
}
