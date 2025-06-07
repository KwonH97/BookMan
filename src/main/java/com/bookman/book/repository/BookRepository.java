package com.bookman.book.repository;

import com.bookman.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * ISBN으로 도서 존재 여부 확인
     */
    boolean existsByIsbn(String isbn);

    /**
     * ISBN으로 도서 조회
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * 제목으로 도서 검색 (부분 일치)
     */
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:title%")
    Optional<Book> findByTitleContaining(@Param("title") String title);

    /**
     * 등록일순으로 모든 도서 조회 (페이징)
     */
    Page<Book> findAllByOrderByRegisteredDateDesc(Pageable pageable);

    /**
     * 제목으로 도서 검색 (부분 일치, 페이징)
     */
    Page<Book> findByTitleContainingIgnoreCaseOrderByRegisteredDateDesc(String title, Pageable pageable);

    /**
     * 출판사로 도서 검색 (부분 일치, 페이징)
     */
    Page<Book> findByPublisherContainingIgnoreCaseOrderByRegisteredDateDesc(String publisher, Pageable pageable);

    /**
     * 제목 또는 출판사로 도서 검색 (통합 검색, 페이징)
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY b.registeredDate DESC")
    Page<Book> findByTitleOrPublisherContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 카테고리별 도서 조회 (페이징)
     */
    Page<Book> findByCategoryIdOrderByRegisteredDateDesc(Integer categoryId, Pageable pageable);
}
