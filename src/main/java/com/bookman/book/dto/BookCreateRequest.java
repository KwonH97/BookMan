package com.bookman.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "도서 등록 요청 DTO")
public class BookCreateRequest {

    @NotBlank(message = "ISBN은 필수입니다")
    @Size(max = 20, message = "ISBN은 20자 이하여야 합니다")
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
            message = "올바른 ISBN 형식이 아닙니다")
    @Schema(description = "ISBN", example = "9788966261208", required = true)
    private String isbn;

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "도서 제목", example = "클린 코드", required = true)
    private String title;

    @Size(max = 100, message = "출판사는 100자 이하여야 합니다")
    @Schema(description = "출판사", example = "인사이트")
    private String publisher;

    @Min(value = 1900, message = "출판연도는 1000년 이후여야 합니다")
    @Max(value = 2100, message = "출판연도는 2100년 이전이어야 합니다")
    @Schema(description = "출판연도", example = "2025")
    private Integer publicationYear;

    @Min(value = 1, message = "카테고리 ID는 1 이상이어야 합니다")
    @Schema(description = "카테고리 ID", example = "1")
    private Integer categoryId;

    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
    @Schema(description = "도서 설명", example = "클린코드 꽤잼씀 읽어주세요.")
    private String description;

    @Size(max = 255, message = "커버 이미지 URL은 255자 이하여야 합니다")
    @Pattern(regexp = "^https?://.*", 
            message = "올바른 URL 형식이 아닙니다 (http:// 또는 https://로 시작해야 함)")
    @Schema(description = "커버 이미지 URL", 
            example = "https://image.yes24.com/goods/11681152/XL",
            pattern = "^https?://.*",
            format = "uri")
    private String coverImageUrl;

    @NotNull(message = "총 수량은 필수입니다")
    @Min(value = 1, message = "총 수량은 1 이상이어야 합니다")
    @Max(value = 99, message = "총 수량은 99 이하여야 합니다")
    @Schema(description = "총 수량", example = "10", required = true)
    private Integer totalQuantity;

    @NotNull(message = "현재 재고는 필수입니다")
    @Min(value = 0, message = "현재 재고는 0 이상이어야 합니다")
    @Schema(description = "현재 재고", example = "10", required = true)
    private Integer currentStock;
}
