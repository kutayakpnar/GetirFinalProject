package com.librarymanagement.librarymanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private LocalDate publicationDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Genre genre;
    
    private String description;
    
    private String publisher;
    
    private Integer pageCount;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private Boolean available = true;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Borrowing> borrowings = new ArrayList<>();
} 