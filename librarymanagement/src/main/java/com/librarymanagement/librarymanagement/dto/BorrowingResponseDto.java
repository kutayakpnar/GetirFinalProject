package com.librarymanagement.librarymanagement.dto;

import com.librarymanagement.librarymanagement.model.BorrowingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing borrowing details")
public class BorrowingResponseDto {
    @Schema(description = "Unique identifier of the borrowing record", example = "1")
    private Long id;
    
    @Schema(description = "ID of the user who borrowed the book", example = "101")
    private Long userId;
    
    @Schema(description = "Name of the user who borrowed the book", example = "John Doe")
    private String userName;
    
    @Schema(description = "ID of the borrowed book", example = "201")
    private Long bookId;
    
    @Schema(description = "Title of the borrowed book", example = "The Great Gatsby")
    private String bookTitle;
    
    @Schema(description = "Author of the borrowed book", example = "F. Scott Fitzgerald")
    private String bookAuthor;
    
    @Schema(description = "Date when the book was borrowed", example = "2024-06-01", type = "string", format = "date")
    private LocalDate borrowDate;
    
    @Schema(description = "Date by which the book should be returned", example = "2024-06-15", type = "string", format = "date")
    private LocalDate dueDate;
    
    @Schema(description = "Date when the book was actually returned (null if not returned yet)", 
            example = "2024-06-10", type = "string", format = "date", nullable = true)
    private LocalDate returnDate;
    
    @Schema(description = "Current status of the borrowing", example = "BORROWED", 
            allowableValues = {"BORROWED", "RETURNED", "OVERDUE"})
    private BorrowingStatus status;
} 