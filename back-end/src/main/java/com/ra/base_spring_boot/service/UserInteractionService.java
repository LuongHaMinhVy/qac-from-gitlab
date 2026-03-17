package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.req.UserInteractionRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;

public interface UserInteractionService {
    ApiResponse<String> interact(UserInteractionRequest request);
}
