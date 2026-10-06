package com.takima.backskeleton.User.DTO;

import org.springframework.data.domain.Page;

import java.util.List;

/** Réponse paginée stable (on ne sérialise pas directement PageImpl). */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
