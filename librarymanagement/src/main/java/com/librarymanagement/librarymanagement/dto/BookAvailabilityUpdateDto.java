package com.librarymanagement.librarymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookAvailabilityUpdateDto {
    private Long bookId;
    private String title;
    private boolean available;
    private LocalDateTime timestamp;
    
    public BookAvailabilityUpdateDto(Long bookId, String title, boolean available) {
        this.bookId = bookId;
        this.title = title;
        this.available = available;
        this.timestamp = LocalDateTime.now();
    }
} 