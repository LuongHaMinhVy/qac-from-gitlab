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
public class FormLogin {
    @NotBlank(message = "Email must not be blank")
    @ValidEmail
    private String email;

    @NotBlank(message = "Password must not be blank")
    @ValidPassword(message = "Password must be at least 8 characters long and include at least 1 letter and 1 number")
    private String password;
}
