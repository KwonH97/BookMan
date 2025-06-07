package com.bookman.book.service;

import com.bookman.book.dto.BookCreateRequest;
import com.bookman.book.dto.BookPageResponse;
import com.bookman.book.dto.BookResponse;
import com.bookman.book.entity.Book;
import com.bookman.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookService {

    private final BookRepository bookRepository;

    /**
     * 도서 등록
     */
    @Transactional
    public BookResponse createBook(BookCreateRequest request) {
        log.info("도서 등록 요청: ISBN={}, 제목={}", request.getIsbn(), request.getTitle());

        // ISBN 중복 체크
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalArgumentException("이미 등록된 ISBN입니다: " + request.getIsbn());
        }

        // 재고 검증
        if (request.getCurrentStock() > request.getTotalQuantity()) {
            throw new IllegalArgumentException("현재 재고는 총 수량보다 클 수 없습니다");
        }

        // Book 엔티티 생성
        Book book = Book.builder()
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

        Book savedBook = bookRepository.save(book);
        log.info("도서 등록 완료: ID={}, ISBN={}", savedBook.getBookId(), savedBook.getIsbn());

        return BookResponse.from(savedBook);
    }

    /**
     * 도서 조회 (ID로)
     */
    public BookResponse getBook(Long bookId) {
        log.info("도서 조회 요청: ID={}", bookId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다: " + bookId));

        return BookResponse.from(book);
    }

    /**
     * 도서 조회 (ISBN으로)
     */
    public BookResponse getBookByIsbn(String isbn) {
        log.info("도서 조회 요청: ISBN={}", isbn);

        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ISBN입니다: " + isbn));

        return BookResponse.from(book);
    }

    /**
     * 도서 목록 조회 (등록일순, 페이징)
     */
    public BookPageResponse getBooks(int page, int size) {
        log.info("도서 목록 조회 요청: page={}, size={}", page, size);

        // 페이지 유효성 검증
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 크기는 1~100 사이여야 합니다");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findAllByOrderByRegisteredDateDesc(pageable);
        
        Page<BookResponse> responsePage = bookPage.map(BookResponse::from);
        
        log.info("도서 목록 조회 완료: 총 {}건, 현재 페이지 {}/{}", 
                responsePage.getTotalElements(), page + 1, responsePage.getTotalPages());

        return BookPageResponse.from(responsePage);
    }

    /**
     * 도서 검색 (제목 기반, 페이징)
     */
    public BookPageResponse searchBooksByTitle(String title, int page, int size) {
        log.info("도서 제목 검색 요청: title={}, page={}, size={}", title, page, size);

        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("검색 키워드는 필수입니다");
        }

        // 페이지 유효성 검증
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 크기는 1~100 사이여야 합니다");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findByTitleContainingIgnoreCaseOrderByRegisteredDateDesc(title, pageable);
        
        Page<BookResponse> responsePage = bookPage.map(BookResponse::from);
        
        log.info("도서 제목 검색 완료: 키워드={}, 검색 결과 {}건", title, responsePage.getTotalElements());

        return BookPageResponse.from(responsePage, title);
    }

    /**
     * 도서 통합 검색 (제목 + 출판사, 페이징)
     */
    public BookPageResponse searchBooks(String keyword, int page, int size) {
        log.info("도서 통합 검색 요청: keyword={}, page={}, size={}", keyword, page, size);

        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("검색 키워드는 필수입니다");
        }

        // 페이지 유효성 검증
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 크기는 1~100 사이여야 합니다");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findByTitleOrPublisherContaining(keyword, pageable);
        
        Page<BookResponse> responsePage = bookPage.map(BookResponse::from);
        
        log.info("도서 통합 검색 완료: 키워드={}, 검색 결과 {}건", keyword, responsePage.getTotalElements());

        return BookPageResponse.from(responsePage, keyword);
    }

    /**
     * 카테고리별 도서 조회 (페이징)
     */
    public BookPageResponse getBooksByCategory(Integer categoryId, int page, int size) {
        log.info("카테고리별 도서 조회 요청: categoryId={}, page={}, size={}", categoryId, page, size);

        if (categoryId == null || categoryId < 1) {
            throw new IllegalArgumentException("유효한 카테고리 ID가 필요합니다");
        }

        // 페이지 유효성 검증
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 크기는 1~100 사이여야 합니다");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findByCategoryIdOrderByRegisteredDateDesc(categoryId, pageable);
        
        Page<BookResponse> responsePage = bookPage.map(BookResponse::from);
        
        log.info("카테고리별 도서 조회 완료: categoryId={}, 검색 결과 {}건", categoryId, responsePage.getTotalElements());

        return BookPageResponse.from(responsePage, categoryId);
    }
}
