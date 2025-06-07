package com.bookman.book.service;

import com.bookman.book.dto.BookCreateRequest;
import com.bookman.book.dto.BookPageResponse;
import com.bookman.book.dto.BookResponse;
import com.bookman.book.entity.Book;
import com.bookman.book.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("BookService 단위 테스트")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Nested
    @DisplayName("도서 등록 테스트")
    class CreateBookTest {

        @Test
        @DisplayName("성공 - 유효한 도서 정보로 등록")
        void createBook_Success() {
            // Given
            BookCreateRequest request = BookCreateRequest.builder()
                    .isbn("9788966261208")
                    .title("클린 코드")
                    .publisher("인사이트")
                    .publicationYear(2013)
                    .categoryId(1)
                    .description("클린코드 꽤잼씀 읽어주세요")
                    .coverImageUrl("https://image.yes24.com/goods/11681152/XL")
                    .totalQuantity(10)
                    .currentStock(10)
                    .build();

            Book savedBook = Book.builder()
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

            given(bookRepository.existsByIsbn(request.getIsbn())).willReturn(false);
            given(bookRepository.save(any(Book.class))).willReturn(savedBook);

            // When
            BookResponse response = bookService.createBook(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBookId()).isEqualTo(1L);
            assertThat(response.getIsbn()).isEqualTo("9788966261208");
            assertThat(response.getTitle()).isEqualTo("클린 코드");
            assertThat(response.getPublisher()).isEqualTo("인사이트");
            assertThat(response.getPublicationYear()).isEqualTo(2013);
            assertThat(response.getCategoryId()).isEqualTo(1);
            assertThat(response.getDescription()).isEqualTo("클린코드 꽤잼씀 읽어주세요");
            assertThat(response.getCoverImageUrl()).isEqualTo("https://image.yes24.com/goods/11681152/XL");
            assertThat(response.getTotalQuantity()).isEqualTo(10);
            assertThat(response.getCurrentStock()).isEqualTo(10);
            assertThat(response.getRegisteredDate()).isNotNull();

            verify(bookRepository).existsByIsbn(request.getIsbn());
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("실패 - 중복된 ISBN")
        void createBook_FailByDuplicateIsbn() {
            // Given
            BookCreateRequest request = BookCreateRequest.builder()
                    .isbn("9788966261208")
                    .title("클린 코드")
                    .publisher("인사이트")
                    .publicationYear(2013)
                    .categoryId(1)
                    .totalQuantity(10)
                    .currentStock(10)
                    .build();

            given(bookRepository.existsByIsbn(request.getIsbn())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 등록된 ISBN입니다: " + request.getIsbn());

            verify(bookRepository).existsByIsbn(request.getIsbn());
        }

        @Test
        @DisplayName("실패 - 현재 재고가 총 수량보다 큰 경우")
        void createBook_FailByInvalidStock() {
            // Given
            BookCreateRequest request = BookCreateRequest.builder()
                    .isbn("9788966261208")
                    .title("클린 코드")
                    .publisher("인사이트")
                    .publicationYear(2013)
                    .categoryId(1)
                    .totalQuantity(10)
                    .currentStock(15) // 총 수량보다 큰 재고
                    .build();

            given(bookRepository.existsByIsbn(request.getIsbn())).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("현재 재고는 총 수량보다 클 수 없습니다");

            verify(bookRepository).existsByIsbn(request.getIsbn());
        }

        @Test
        @DisplayName("성공 - 최소 필수 정보만으로 등록")
        void createBook_SuccessWithMinimalInfo() {
            // Given
            BookCreateRequest request = BookCreateRequest.builder()
                    .isbn("9788966261208")
                    .title("클린 코드")
                    .totalQuantity(1)
                    .currentStock(1)
                    .build();

            Book savedBook = Book.builder()
                    .bookId(1L)
                    .isbn(request.getIsbn())
                    .title(request.getTitle())
                    .totalQuantity(request.getTotalQuantity())
                    .currentStock(request.getCurrentStock())
                    .registeredDate(LocalDateTime.now())
                    .build();

            given(bookRepository.existsByIsbn(request.getIsbn())).willReturn(false);
            given(bookRepository.save(any(Book.class))).willReturn(savedBook);

            // When
            BookResponse response = bookService.createBook(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBookId()).isEqualTo(1L);
            assertThat(response.getIsbn()).isEqualTo("9788966261208");
            assertThat(response.getTitle()).isEqualTo("클린 코드");
            assertThat(response.getPublisher()).isNull();
            assertThat(response.getPublicationYear()).isNull();
            assertThat(response.getCategoryId()).isNull();
            assertThat(response.getDescription()).isNull();
            assertThat(response.getCoverImageUrl()).isNull();
            assertThat(response.getTotalQuantity()).isEqualTo(1);
            assertThat(response.getCurrentStock()).isEqualTo(1);

            verify(bookRepository).existsByIsbn(request.getIsbn());
            verify(bookRepository).save(any(Book.class));
        }
    }

    @Nested
    @DisplayName("도서 조회 테스트")
    class GetBookTest {

        @Test
        @DisplayName("성공 - ID로 도서 조회")
        void getBook_SuccessById() {
            // Given
            Long bookId = 1L;
            Book book = Book.builder()
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

            given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

            // When
            BookResponse response = bookService.getBook(bookId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBookId()).isEqualTo(bookId);
            assertThat(response.getIsbn()).isEqualTo("9788966261208");
            assertThat(response.getTitle()).isEqualTo("클린 코드");
            assertThat(response.getCurrentStock()).isEqualTo(8);

            verify(bookRepository).findById(bookId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 도서 ID")
        void getBook_FailByNotFound() {
            // Given
            Long bookId = 999L;
            given(bookRepository.findById(bookId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookService.getBook(bookId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 도서입니다: " + bookId);

            verify(bookRepository).findById(bookId);
        }

        @Test
        @DisplayName("성공 - ISBN으로 도서 조회")
        void getBookByIsbn_Success() {
            // Given
            String isbn = "9788966261208";
            Book book = Book.builder()
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

            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.of(book));

            // When
            BookResponse response = bookService.getBookByIsbn(isbn);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBookId()).isEqualTo(1L);
            assertThat(response.getIsbn()).isEqualTo(isbn);
            assertThat(response.getTitle()).isEqualTo("클린 코드");
            assertThat(response.getCurrentStock()).isEqualTo(8);

            verify(bookRepository).findByIsbn(isbn);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 ISBN")
        void getBookByIsbn_FailByNotFound() {
            // Given
            String isbn = "9999999999999";
            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookService.getBookByIsbn(isbn))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 ISBN입니다: " + isbn);

            verify(bookRepository).findByIsbn(isbn);
        }
    }

    @Nested
    @DisplayName("도서 목록 조회 및 검색 테스트")
    class BookListAndSearchTest {

        @Test
        @DisplayName("성공 - 도서 목록 조회 (페이징)")
        void getBooks_Success() {
            // Given
            int page = 0, size = 10;
            List<Book> books = Arrays.asList(
                    createSampleBook(1L, "9788966261208", "클린 코드"),
                    createSampleBook(2L, "9788966261209", "리팩터링")
            );
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(page, size), 2);

            given(bookRepository.findAllByOrderByRegisteredDateDesc(any(Pageable.class)))
                    .willReturn(bookPage);

            // When
            BookPageResponse response = bookService.getBooks(page, size);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBooks()).hasSize(2);
            assertThat(response.getCurrentPage()).isEqualTo(0);
            assertThat(response.getPageSize()).isEqualTo(10);
            assertThat(response.getTotalBooks()).isEqualTo(2);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();

            verify(bookRepository).findAllByOrderByRegisteredDateDesc(any(Pageable.class));
        }

        @Test
        @DisplayName("실패 - 잘못된 페이지 번호")
        void getBooks_FailByInvalidPageNumber() {
            // Given
            int page = -1, size = 10;

            // When & Then
            assertThatThrownBy(() -> bookService.getBooks(page, size))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("페이지 번호는 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("실패 - 잘못된 페이지 크기")
        void getBooks_FailByInvalidPageSize() {
            // Given
            int page = 0, size = 101;

            // When & Then
            assertThatThrownBy(() -> bookService.getBooks(page, size))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("페이지 크기는 1~100 사이여야 합니다");
        }

        @Test
        @DisplayName("성공 - 제목으로 도서 검색")
        void searchBooksByTitle_Success() {
            // Given
            String title = "클린";
            int page = 0, size = 10;
            List<Book> books = Arrays.asList(
                    createSampleBook(1L, "9788966261208", "클린 코드"),
                    createSampleBook(2L, "9788966261209", "클린 아키텍처")
            );
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(page, size), 2);

            given(bookRepository.findByTitleContainingIgnoreCaseOrderByRegisteredDateDesc(eq(title), any(Pageable.class)))
                    .willReturn(bookPage);

            // When
            BookPageResponse response = bookService.searchBooksByTitle(title, page, size);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBooks()).hasSize(2);
            assertThat(response.getSearchKeyword()).isEqualTo(title);
            assertThat(response.getTotalBooks()).isEqualTo(2);

            verify(bookRepository).findByTitleContainingIgnoreCaseOrderByRegisteredDateDesc(eq(title), any(Pageable.class));
        }

        @Test
        @DisplayName("실패 - 빈 검색 키워드")
        void searchBooksByTitle_FailByEmptyKeyword() {
            // Given
            String title = "";
            int page = 0, size = 10;

            // When & Then
            assertThatThrownBy(() -> bookService.searchBooksByTitle(title, page, size))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("검색 키워드는 필수입니다");
        }

        @Test
        @DisplayName("성공 - 통합 검색 (제목 + 출판사)")
        void searchBooks_Success() {
            // Given
            String keyword = "인사이트";
            int page = 0, size = 10;
            List<Book> books = Arrays.asList(
                    createSampleBook(1L, "9788966261208", "클린 코드"),
                    createSampleBook(2L, "9788966261209", "리팩터링")
            );
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(page, size), 2);

            given(bookRepository.findByTitleOrPublisherContaining(eq(keyword), any(Pageable.class)))
                    .willReturn(bookPage);

            // When
            BookPageResponse response = bookService.searchBooks(keyword, page, size);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBooks()).hasSize(2);
            assertThat(response.getSearchKeyword()).isEqualTo(keyword);
            assertThat(response.getTotalBooks()).isEqualTo(2);

            verify(bookRepository).findByTitleOrPublisherContaining(eq(keyword), any(Pageable.class));
        }

        @Test
        @DisplayName("성공 - 카테고리별 도서 조회")
        void getBooksByCategory_Success() {
            // Given
            Integer categoryId = 1;
            int page = 0, size = 10;
            List<Book> books = Arrays.asList(
                    createSampleBook(1L, "9788966261208", "클린 코드"),
                    createSampleBook(2L, "9788966261209", "리팩터링")
            );
            Page<Book> bookPage = new PageImpl<>(books, PageRequest.of(page, size), 2);

            given(bookRepository.findByCategoryIdOrderByRegisteredDateDesc(eq(categoryId), any(Pageable.class)))
                    .willReturn(bookPage);

            // When
            BookPageResponse response = bookService.getBooksByCategory(categoryId, page, size);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBooks()).hasSize(2);
            assertThat(response.getCategoryId()).isEqualTo(categoryId);
            assertThat(response.getTotalBooks()).isEqualTo(2);

            verify(bookRepository).findByCategoryIdOrderByRegisteredDateDesc(eq(categoryId), any(Pageable.class));
        }

        @Test
        @DisplayName("실패 - 잘못된 카테고리 ID")
        void getBooksByCategory_FailByInvalidCategoryId() {
            // Given
            Integer categoryId = 0;
            int page = 0, size = 10;

            // When & Then
            assertThatThrownBy(() -> bookService.getBooksByCategory(categoryId, page, size))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유효한 카테고리 ID가 필요합니다");
        }

        @Test
        @DisplayName("성공 - 빈 검색 결과")
        void searchBooks_SuccessWithEmptyResult() {
            // Given
            String keyword = "존재하지않는책";
            int page = 0, size = 10;
            Page<Book> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);

            given(bookRepository.findByTitleOrPublisherContaining(eq(keyword), any(Pageable.class)))
                    .willReturn(emptyPage);

            // When
            BookPageResponse response = bookService.searchBooks(keyword, page, size);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBooks()).isEmpty();
            assertThat(response.getTotalBooks()).isEqualTo(0);
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.getSearchKeyword()).isEqualTo(keyword);

            verify(bookRepository).findByTitleOrPublisherContaining(eq(keyword), any(Pageable.class));
        }

        private Book createSampleBook(Long id, String isbn, String title) {
            return Book.builder()
                    .bookId(id)
                    .isbn(isbn)
                    .title(title)
                    .publisher("인사이트")
                    .publicationYear(2013)
                    .categoryId(1)
                    .description("테스트 설명")
                    .totalQuantity(10)
                    .currentStock(10)
                    .registeredDate(LocalDateTime.now())
                    .build();
        }
    }
}
