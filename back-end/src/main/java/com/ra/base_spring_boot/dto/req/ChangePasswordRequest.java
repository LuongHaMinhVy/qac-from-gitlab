package com.ra.base_spring_boot.dto.req;

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
public class ChangePasswordRequest {

    @NotBlank(message = "Old password must not be blank")
    @ValidPassword
    private String oldPassword;

    @NotBlank(message = "New password must not be blank")
    @ValidPassword
    private String newPassword;

    @NotBlank(message = "Confirm password must not be blank")
    @ValidPassword
    private String confirmPassword;
}
