package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.validate.ValidEmail;
import com.ra.base_spring_boot.validate.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateMemberAccountRequest {

    @NotBlank(message = "Email must not be blank")
    @ValidEmail
    private String email;

    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @ValidPassword
    private String password;

    @NotBlank(message = "Full name must not be blank")
    private String fullName;
}
