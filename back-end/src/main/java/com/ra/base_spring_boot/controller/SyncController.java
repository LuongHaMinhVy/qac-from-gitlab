package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.SyncResponseDTO;
import com.ra.base_spring_boot.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Tag(name = "Sync", description = "Đồng bộ dữ liệu khởi tạo (Web/Mobile)")
public class SyncController {

    private final SyncService syncService;

    @GetMapping
    @Operation(summary = "Lấy dữ liệu đồng bộ", description = "Trả về thông tin user, thông báo, menu, danh mục và cài đặt trong một request duy nhất.")
    public ResponseEntity<ApiResponse<SyncResponseDTO>> getSyncData() {
        return ResponseEntity.ok(syncService.getSyncData());
    }
}
