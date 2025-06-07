package com.bookman.book.controller;

import com.bookman.book.dto.BookCreateRequest;
import com.bookman.book.dto.BookResponse;
import com.bookman.book.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Tag("integration")
@DisplayName("BookController 통합 테스트")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Nested
    @DisplayName("도서 등록 API 테스트")
    class CreateBookApiTest {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("성공 - 관리자 권한으로 도서 등록")
        void createBook_Success() throws Exception {
            // Given
            BookCreateRequest request = BookCreateRequest.builder()
                    .isbn("9788966261208")
                    .title("클린 코드")
                    .publisher("인사이트")
                    .publicationYear(2013)
                    .categoryId(1)
                    .description("클린코드 꽤잼씀여 읽어주세요")
                    .coverImageUrl("https://image.yes24.com/goods/11681152/XL")
                    .totalQuantity(10)
                    .currentStock(10)
                    .build();

            BookResponse response = BookResponse.builder()
                    .bookId(1L)
                    .isbn(request.getIsbn())
                    .title(request.getTitle())
                    .publisher(request.getPublisher())
                    .publicationYear(request.getPublicationYear())
                    .categoryId(request.getCategoryId())
                    .description(request.getDescription())
                    .coverImageUrl(request.getCoverImageUrl())
                    .totalQuantity(request.getTotalQuantity())
                    .currentStock(request.getCurrentStock())
                    .registeredDate(LocalDateTime.now())
                    .build();

            given(bookService.createBook(any(BookCreateRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.bookId").value(1L))
                    .andExpect(jsonPath("$.isbn").value("9788966261208"))
                    .andExpect(jsonPath("$.title").value("클린 코드"))
                    .andExpect(jsonPath("$.publisher").value("인사이트"))
                    .andExpect(jsonPath("$.publicationYear").value(2013))
                    .andExpect(jsonPath("$.categoryId").value(1))
                    .andExpect(jsonPath("$.description").value("클린코드 꽤잼씀여 읽어주세요"))
                    .andExpect(jsonPath("$.coverImageUrl").value("https://image.yes24.com/goods/11681152/XL"))
                    .andExpect(jsonPath("$.totalQuantity").value(10))
                    .andExpect(jsonPath("$.currentStock").value(10))
                    .andExpect(jsonPath("$.registeredDate").exists());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("실패 - 일반 사용자 권한으로 도서 등록 시도")
        void createBook_FailByInsufficientAuthority() throws Exception {
            // Given
            BookCreateRequest request = BookCreateRequest.builder()
                    .isbn("9788966261208")
                    .title("클린 코드")
                    .totalQuantity(10)
                    .currentStock(10)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void createBook_FailByUnauthenticated() throws Exception {
            // Given
            BookCreateRequest request = BookCreateRequest.builder()
                    .isbn("9788966261208")
                    .title("클린 코드")
                    .totalQuantity(10)
                    .currentStock(10)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("실패 - 필수 필드 누락")
        void createBook_FailByValidation() throws Exception {
            // Given - ISBN이 누락된 요청
            BookCreateRequest request = BookCreateRequest.builder()
                    .title("클린 코드")
                    .totalQuantity(10)
                    .currentStock(10)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("실패 - 잘못된 ISBN 형식")
        void createBook_FailByInvalidIsbn() throws Exception {
            // Given
            BookCreateRequest request = BookCreateRequest.builder()
                    .isbn("invalid-isbn")
                    .title("클린 코드")
                    .totalQuantity(10)
                    .currentStock(10)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("올바른 ISBN 형식이 아닙니다")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("실패 - 서비스 레이어에서 예외 발생")
        void createBook_FailByServiceException() throws Exception {
            // Given
            BookCreateRequest request = BookCreateRequest.builder()
                    .isbn("9788966261208")
                    .title("클린 코드")
                    .totalQuantity(10)
                    .currentStock(10)
                    .build();

            given(bookService.createBook(any(BookCreateRequest.class)))
                    .willThrow(new IllegalArgumentException("이미 등록된 ISBN입니다: 9788966261208"));

            // When & Then
            mockMvc.perform(post("/api/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("이미 등록된 ISBN입니다: 9788966261208"));
        }
    }

    @Nested
    @DisplayName("도서 조회 API 테스트")
    class GetBookApiTest {

        @Test
        @WithMockUser
        @DisplayName("성공 - ID로 도서 조회")
        void getBook_SuccessById() throws Exception {
            // Given
            Long bookId = 1L;
            BookResponse response = BookResponse.builder()
                    .bookId(bookId)
                    .isbn("9788966261208")
                    .title("클린 코드")
                    .publisher("인사이트")
                    .publicationYear(2013)
                    .categoryId(1)
                    .description("클린코드 꽤잼씀여 읽어주세요")
                    .coverImageUrl("https://image.yes24.com/goods/11681152/XL")
                    .totalQuantity(10)
                    .currentStock(8)
                    .registeredDate(LocalDateTime.now())
                    .build();

            given(bookService.getBook(eq(bookId))).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/books/{bookId}", bookId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.bookId").value(bookId))
                    .andExpect(jsonPath("$.isbn").value("9788966261208"))
                    .andExpect(jsonPath("$.title").value("클린 코드"))
                    .andExpect(jsonPath("$.currentStock").value(8));
        }

        @Test
        @WithMockUser
        @DisplayName("성공 - ISBN으로 도서 조회")
        void getBookByIsbn_Success() throws Exception {
            // Given
            String isbn = "9788966261208";
            BookResponse response = BookResponse.builder()
                    .bookId(1L)
                    .isbn(isbn)
                    .title("클린 코드")
                    .publisher("인사이트")
                    .publicationYear(2013)
                    .categoryId(1)
                    .description("클린코드 꽤잼씀여 읽어주세요")
                    .coverImageUrl("https://image.yes24.com/goods/11681152/XL")
                    .totalQuantity(10)
                    .currentStock(8)
                    .registeredDate(LocalDateTime.now())
                    .build();

            given(bookService.getBookByIsbn(eq(isbn))).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/books/isbn/{isbn}", isbn))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.bookId").value(1L))
                    .andExpect(jsonPath("$.isbn").value(isbn))
                    .andExpect(jsonPath("$.title").value("클린 코드"));
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자의 도서 조회")
        void getBook_FailByUnauthenticated() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/books/{bookId}", 1L))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("실패 - 존재하지 않는 도서 조회")
        void getBook_FailByNotFound() throws Exception {
            // Given
            Long bookId = 999L;
            given(bookService.getBook(eq(bookId)))
                    .willThrow(new IllegalArgumentException("존재하지 않는 도서입니다: " + bookId));

            // When & Then
            mockMvc.perform(get("/api/books/{bookId}", bookId))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 도서입니다: " + bookId));
        }

        @Test
        @WithMockUser
        @DisplayName("실패 - 존재하지 않는 ISBN으로 조회")
        void getBookByIsbn_FailByNotFound() throws Exception {
            // Given
            String isbn = "9999999999999";
            given(bookService.getBookByIsbn(eq(isbn)))
                    .willThrow(new IllegalArgumentException("존재하지 않는 ISBN입니다: " + isbn));

            // When & Then
            mockMvc.perform(get("/api/books/isbn/{isbn}", isbn))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 ISBN입니다: " + isbn));
        }
    }
}
