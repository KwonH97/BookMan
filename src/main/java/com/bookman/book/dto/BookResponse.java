package com.bookman.book.dto;

import com.bookman.book.entity.Book;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "도서 응답 DTO")
public class BookResponse {

    @Schema(description = "도서 ID", example = "1")
    private Long bookId;

    @Schema(description = "ISBN", example = "9788966261208")
    private String isbn;

    @Schema(description = "도서 제목", example = "클린 코드")
    private String title;

    @Schema(description = "출판사", example = "인사이트")
    private String publisher;

    @Schema(description = "출판연도", example = "2013")
    private Integer publicationYear;

    @Schema(description = "카테고리 ID", example = "1")
    private Integer categoryId;

    @Schema(description = "도서 설명", example = "클린코드 꽤잼씀 읽어주세요.")
    private String description;

    @Schema(description = "커버 이미지 URL", example = "https://image.yes24.com/goods/11681152/XL")
    private String coverImageUrl;

    @Schema(description = "총 수량", example = "10")
    private Integer totalQuantity;

    @Schema(description = "현재 재고", example = "10")
    private Integer currentStock;

    @Schema(description = "등록일시", example = "2025-01-01T12:00:00")
    private LocalDateTime registeredDate;

    public static BookResponse from(Book book) {
        return BookResponse.builder()
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .categoryId(book.getCategoryId())
                .description(book.getDescription())
                .coverImageUrl(book.getCoverImageUrl())
                .totalQuantity(book.getTotalQuantity())
                .currentStock(book.getCurrentStock())
                .registeredDate(book.getRegisteredDate())
                .build();
    }
}
