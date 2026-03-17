package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.validate.ValidEmail;
import com.ra.base_spring_boot.validate.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FormRegister {
    @ValidEmail(message = "Invalid email format")
    @NotBlank(message = "Password must not be blank")
    private String email;
    @NotBlank(message = " must not be blank")
    private String username;
    @NotBlank(message = "Password must not be blank")
    @ValidPassword(message = "Password must be at least 8 characters long and include at least 1 letter and 1 number")
    private String password;
}
