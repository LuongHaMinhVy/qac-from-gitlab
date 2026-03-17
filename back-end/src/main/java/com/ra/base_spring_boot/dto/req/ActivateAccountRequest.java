package com.ra.base_spring_boot.dto.req;

import lombok.Data;

@Data
public class ActivateAccountRequest {
    private String email;
    private String otp;
}
