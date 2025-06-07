package com.bookman.book.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE `Book` SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", length = 255)
    private String coverImageUrl;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 1;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 1;

    @Column(name = "registered_date", nullable = false)
    private LocalDateTime registeredDate = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Soft delete 필드
}
