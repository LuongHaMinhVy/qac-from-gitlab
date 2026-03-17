package com.ra.base_spring_boot.dto.resp;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponseDTO {
    private Integer id;
    private String username;
    private String email;
    private Boolean status;
    private List<String> roles;
    private List<String> permissions;
}