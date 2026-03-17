package com.ra.base_spring_boot.dto.resp;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncResponseDTO {
    private MemberResponse user;
    private Long unreadNotificationCount;
    private List<MenuResponse> menus;
    private List<CategoryResponseDTO> categories;
    private List<SystemSettingResponse> settings;
}
