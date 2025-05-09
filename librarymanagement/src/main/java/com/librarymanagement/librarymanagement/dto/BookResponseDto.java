package com.librarymanagement.librarymanagement.dto;

import com.librarymanagement.librarymanagement.model.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponseDto {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private LocalDate publicationDate;
    private Genre genre;
    private String description;
    private String publisher;
    private Integer pageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean available;
} 