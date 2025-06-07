package com.bookman.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "도서 목록 페이징 응답 DTO")
public class BookPageResponse {

    @Schema(description = "도서 목록", example = "[{도서 정보}]")
    private List<BookResponse> books;

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int currentPage;

    @Schema(description = "페이지 크기", example = "20")
    private int pageSize;

    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPages;

    @Schema(description = "전체 도서 수", example = "95")
    private long totalBooks;

    @Schema(description = "첫 번째 페이지 여부", example = "true")
    private boolean isFirst;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean isLast;

    @Schema(description = "검색 키워드 (검색 시에만)", example = "클린")
    private String searchKeyword;

    @Schema(description = "카테고리 ID (카테고리 검색 시에만)", example = "1")
    private Integer categoryId;

    public static BookPageResponse from(Page<BookResponse> page) {
        return BookPageResponse.builder()
                .books(page.getContent())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalBooks(page.getTotalElements())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    public static BookPageResponse from(Page<BookResponse> page, String searchKeyword) {
        BookPageResponse response = from(page);
        response.setSearchKeyword(searchKeyword);
        return response;
    }

    public static BookPageResponse from(Page<BookResponse> page, Integer categoryId) {
        BookPageResponse response = from(page);
        response.setCategoryId(categoryId);
        return response;
    }
}
