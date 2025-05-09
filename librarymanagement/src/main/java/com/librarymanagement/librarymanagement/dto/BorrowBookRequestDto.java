package com.librarymanagement.librarymanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for borrowing a book")
public class BorrowBookRequestDto {
    
    @NotNull(message = "Book ID is required")
    @Schema(description = "ID of the book to borrow", example = "1", required = true)
    private Long bookId;
    
    @Future(message = "Due date must be in the future")
    @Schema(description = "Expected return date (optional - default value will be set by the service)", 
           example = "2024-12-31", type = "string", format = "date")
    private LocalDate dueDate; // Eğer belirtilmezse, default değer servis tarafında set edilecek
} 