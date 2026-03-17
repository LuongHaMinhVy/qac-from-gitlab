package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TagRequest {

    @NotEmpty(message = "Danh sách tags không được để trống")
    private List<@Size(min = 1, max = 100, message = "Mỗi tag phải có từ 1-100 ký tự") String> tags;
}
