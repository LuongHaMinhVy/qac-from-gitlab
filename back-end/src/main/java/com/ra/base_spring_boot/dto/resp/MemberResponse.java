package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MemberResponse {
    private Long userId;
    private Integer accountId;
    private String fullName;
    private String phone;
    private MediaResponseDTO avatar;
    private String bio;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
