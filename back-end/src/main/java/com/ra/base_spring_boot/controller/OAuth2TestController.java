package com.ra.base_spring_boot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


@Controller
@Slf4j
public class OAuth2TestController {


    @GetMapping("/oauth2/test-result")
    public String testResult(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String tokenType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String avatar,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String isNewUser,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String error,
            Map<String, Object> model) {

        log.info("=== OAuth2TestController.testResult called ===");
        log.info("Token: {}", token != null ? "present" : "null");
        log.info("Username: {}", username);
        log.info("Email: {}", email);
        log.info("Error: {}", error);

        if (error != null) {
            log.warn("OAuth2 error: {}", error);
            model.put("error", error);
            return "oauth2-error";
        }

        model.put("token", token);
        model.put("tokenType", tokenType != null ? tokenType : "Bearer");
        model.put("username", username);
        model.put("email", email);
        model.put("fullName", fullName);
        model.put("avatar", avatar);
        model.put("provider", provider);
        model.put("isNewUser", isNewUser);
        model.put("message", message);

        return "oauth2-success";
    }
}