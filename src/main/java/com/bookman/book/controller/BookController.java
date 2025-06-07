package com.bookman.book.controller;

import com.bookman.book.dto.BookCreateRequest;
import com.bookman.book.dto.BookPageResponse;
import com.bookman.book.dto.BookResponse;
import com.bookman.book.service.BookService;
import com.bookman.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "📚 Book Management", 
    description = """
        ## 📖 도서 관리 시스템
        
        도서관의 장서 관리를 위한 핵심 API 모음입니다.
        
        ### 🎯 현재 제공 기능
        - **📚 도서 등록**: 새로운 도서를 시스템에 추가
        - **🔍 도서 조회**: ID 또는 ISBN으로 도서 정보 검색
        - **✏️ 도서 수정**: 기존 도서 정보 업데이트 *(개발 예정)*
        - **🗑️ 도서 삭제**: 도서 정보를 안전하게 제거 (Soft Delete) *(개발 예정)*
        
        ### 🚧 추후 구현 예정 기능
        
        #### 📖 도서 관리
        - **🔗 ISBN 자동 입력**: Open API 연동으로 ISBN 기반 도서 정보 자동 완성
        - **📊 대량 등록**: 엑셀 파일 업로드를 통한 도서 일괄 등록
        - **⚠️ 도서 상태 관리**: 분실/파손 도서 처리 및 상태 추적
        
        #### 📊 관리 기능
        - **📝 도서 이력**: 등록/수정/삭제 이력 추적
        
        ### 🔐 권한 요구사항
        | 기능 | 필요 권한 | 설명 |
        |------|-----------|------|
        | 도서 조회 | `USER` | 모든 인증된 사용자 |
        | 도서 등록 | `ADMIN` | 관리자만 가능 |
        | 도서 수정 | `ADMIN` | 관리자만 가능 |
        | 도서 삭제 | `ADMIN` | 관리자만 가능 |
        | 카테고리 관리 | `ADMIN` | 관리자만 가능 |
        | 대량 등록 | `ADMIN` | 관리자만 가능 |
        | 상태 관리 | `ADMIN` | 관리자만 가능 |

        """
)
@SecurityRequirement(name = "JWT")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @Operation(
            summary = "도서 등록",
            description = """
                    새로운 도서를 시스템에 등록합니다.
                    
                    ### 🔒 권한 요구사항
                    - **관리자 권한** 필요 (ROLE_ADMIN)
                    
                    ### ✅ 검증 사항
                    - ISBN 중복 체크
                    - 재고 수량 검증 (현재 재고 ≤ 총 수량)
                    - 필수 필드 검증
                    
                    ### 📝 참고사항
                    - ISBN은 10자리 또는 13자리 형식
                    - 이미지 URL은 선택사항
                    - 등록일시는 자동 설정
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "도서 등록 성공",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (중복 ISBN, 잘못된 데이터 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증이 필요합니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한이 필요합니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookCreateRequest request) {
        
        log.info("도서 등록 API 호출: ISBN={}, 제목={}", request.getIsbn(), request.getTitle());
        
        BookResponse response = bookService.createBook(request);
        
        log.info("도서 등록 API 응답: ID={}", response.getBookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "도서 목록 조회",
            description = """
                    등록일 기준 내림차순으로 도서 목록을 조회합니다.
                    
                    ### 🔒 권한 요구사항
                    - **인증된 사용자** (관리자/일반 사용자 모두 가능)
                    
                    ### 📋 페이징 정보
                    - **기본 페이지 크기**: 20개
                    - **최대 페이지 크기**: 100개
                    - **페이지 번호**: 0부터 시작
                    
                    ### 📊 정렬 기준
                    - 등록일 기준 내림차순 (최신 등록 도서부터)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "도서 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BookPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 페이징 파라미터",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증이 필요합니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<BookPageResponse> getBooks(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "페이지 크기 (1~100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("도서 목록 조회 API 호출: page={}, size={}", page, size);
        
        BookPageResponse response = bookService.getBooks(page, size);
        
        log.info("도서 목록 조회 API 응답: 총 {}건, 현재 페이지 {}/{}",
                response.getTotalBooks(), page + 1, response.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(
            summary = "도서 통합 검색",
            description = """
                    제목과 출판사를 대상으로 통합 검색을 수행합니다.
                    
                    ### 🔍 검색 범위
                    - **제목**: 부분 일치 검색 (대소문자 무시)
                    - **출판사**: 부분 일치 검색 (대소문자 무시)
                    
                    ### 🔒 권한 요구사항
                    - **인증된 사용자** (관리자/일반 사용자 모두 가능)
                    
                    ### 📊 정렬 기준
                    - 등록일 기준 내림차순 (최신 등록 도서부터)
                    
                    ### 💡 검색 팁
                    - 한글, 영문 모두 지원
                    - 공백도 검색 가능
                    - 최소 1자 이상 입력 필요
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "도서 검색 성공",
                    content = @Content(schema = @Schema(implementation = BookPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 검색 파라미터",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증이 필요합니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<BookPageResponse> searchBooks(
            @Parameter(description = "검색 키워드 (제목, 출판사)", example = "클린", required = true)
            @RequestParam String keyword,
            
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "페이지 크기 (1~100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("도서 통합 검색 API 호출: keyword={}, page={}, size={}", keyword, page, size);
        
        BookPageResponse response = bookService.searchBooks(keyword, page, size);
        
        log.info("도서 통합 검색 API 응답: 키워드={}, 검색 결과 {}건",
                keyword, response.getTotalBooks());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/title")
    @Operation(
            summary = "도서 제목 검색",
            description = """
                    도서 제목을 대상으로 검색을 수행합니다.
                    
                    ### 🔍 검색 방식
                    - **부분 일치**: 입력한 키워드가 포함된 모든 도서
                    - **대소문자 무시**: 영문 대소문자 구분 없음
                    
                    ### 🔒 권한 요구사항
                    - **인증된 사용자** (관리자/일반 사용자 모두 가능)
                    """
    )
    public ResponseEntity<BookPageResponse> searchBooksByTitle(
            @Parameter(description = "제목 검색 키워드", example = "클린 코드", required = true)
            @RequestParam String title,
            
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "페이지 크기 (1~100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("도서 제목 검색 API 호출: title={}, page={}, size={}", title, page, size);
        
        BookPageResponse response = bookService.searchBooksByTitle(title, page, size);
        
        log.info("도서 제목 검색 API 응답: 키워드={}, 검색 결과 {}건",
                title, response.getTotalBooks());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookId}")
    @Operation(
            summary = "도서 조회 (ID)",
            description = """
                    도서 ID로 특정 도서 정보를 조회합니다.
                    
                    ### 🔒 권한 요구사항
                    - 인증된 사용자 (관리자/일반 사용자 모두 가능)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "도서 조회 성공",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 도서",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증이 필요합니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<BookResponse> getBook(
            @Parameter(description = "도서 ID", example = "1")
            @PathVariable Long bookId) {
        
        log.info("도서 조회 API 호출: ID={}", bookId);
        
        BookResponse response = bookService.getBook(bookId);
        
        log.info("도서 조회 API 응답: ISBN={}", response.getIsbn());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(
            summary = "도서 조회 (ISBN)",
            description = """
                    ISBN으로 특정 도서 정보를 조회합니다.
                    
                    ### 🔒 권한 요구사항
                    - 인증된 사용자 (관리자/일반 사용자 모두 가능)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "도서 조회 성공",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 ISBN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증이 필요합니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<BookResponse> getBookByIsbn(
            @Parameter(description = "ISBN", example = "9788966261208")
            @PathVariable String isbn) {
        
        log.info("도서 조회 API 호출: ISBN={}", isbn);
        
        BookResponse response = bookService.getBookByIsbn(isbn);
        
        log.info("도서 조회 API 응답: ID={}", response.getBookId());
        return ResponseEntity.ok(response);
    }
}
