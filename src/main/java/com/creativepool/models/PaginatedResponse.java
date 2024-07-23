package com.creativepool.models;

import lombok.Data;

import java.util.List;

@Data
public class PaginatedResponse<T> {
    private long totalElements;
    private List<T> items;
    private boolean isLastPage;
    private Integer currentPage;
    private Integer totalPages;

    public PaginatedResponse(long totalElements, List<T> items, boolean isLastPage, Integer currentPage, Integer totalPages) {
        this.totalElements = totalElements;
        this.items = items;
        this.isLastPage = isLastPage;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }
}
