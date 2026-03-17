package com.ra.base_spring_boot.dto.req;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "Token must not be empty")
    private String token;

    @NotBlank(message = "Please enter a new password")
    private String newPassword;
}
