package com.ra.base_spring_boot.dto.resp;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pagination {
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
}
