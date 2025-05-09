package com.librarymanagement.librarymanagement.controller;

import com.librarymanagement.librarymanagement.dto.BorrowBookRequestDto;
import com.librarymanagement.librarymanagement.dto.BorrowingResponseDto;
import com.librarymanagement.librarymanagement.model.BorrowingStatus;
import com.librarymanagement.librarymanagement.security.UserPrincipal;
import com.librarymanagement.librarymanagement.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
@Tag(name = "Borrowing Management", description = "APIs for managing book borrowings in the library system")
public class BorrowingController {

    private static final Logger logger = LoggerFactory.getLogger(BorrowingController.class);
    
    private final BorrowingService borrowingService;

    @PostMapping("/borrow")
    @PreAuthorize("hasRole('PATRON')")
    @Operation(summary = "Borrow a book", description = "Creates a new borrowing record for a book (requires PATRON role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book borrowed successfully",
                content = @Content(schema = @Schema(implementation = BorrowingResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Book or user not found"),
        @ApiResponse(responseCode = "409", description = "Book not available for borrowing or borrowing limit exceeded")
    })
    public ResponseEntity<BorrowingResponseDto> borrowBook(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Book borrowing details", required = true)
            @Valid @RequestBody BorrowBookRequestDto requestDto) {
        
        logger.info("Request to borrow book received: User ID: {}, Book ID: {}", 
                userPrincipal.getId(), requestDto.getBookId());
        
        BorrowingResponseDto response = borrowingService.borrowBook(userPrincipal.getId(), requestDto);
        
        logger.info("Book successfully borrowed: Borrowing ID: {}, User ID: {}, Book ID: {}", 
                response.getId(), response.getUserId(), response.getBookId());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{borrowingId}/return")
    @PreAuthorize("hasRole('PATRON')")
    @Operation(summary = "Return a book", description = "Returns a borrowed book (requires PATRON role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book returned successfully"),
        @ApiResponse(responseCode = "404", description = "Borrowing record or user not found"),
        @ApiResponse(responseCode = "400", description = "Invalid borrowing state or user does not own this borrowing")
    })
    public ResponseEntity<BorrowingResponseDto> returnBook(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "ID of the borrowing record", required = true)
            @PathVariable Long borrowingId) {
        
        logger.info("Request to return book received: User ID: {}, Borrowing ID: {}", 
                userPrincipal.getId(), borrowingId);
        
        BorrowingResponseDto response = borrowingService.returnBook(userPrincipal.getId(), borrowingId);
        
        logger.info("Book successfully returned: Borrowing ID: {}, Book: {}", 
                response.getId(), response.getBookTitle());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATRON')")
    @Operation(summary = "Get current user's borrowings", 
               description = "Retrieves all borrowings for the authenticated user (requires PATRON role)")
    @ApiResponse(responseCode = "200", description = "Borrowings retrieved successfully")
    public ResponseEntity<Page<BorrowingResponseDto>> getMyBorrowings(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = "borrowDate") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "desc") String direction) {
        
        logger.info("Request for user borrowings: User ID: {}, Page: {}, Size: {}, Sort: {} {}", 
                userPrincipal.getId(), page, size, sortBy, direction);
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<BorrowingResponseDto> borrowings = borrowingService.getUserBorrowings(userPrincipal.getId(), pageable);
        
        logger.info("Retrieved {} borrowings for user: {} (Page {} of {})", 
                borrowings.getNumberOfElements(), userPrincipal.getId(), 
                borrowings.getNumber(), borrowings.getTotalPages());
        
        return ResponseEntity.ok(borrowings);
    }

    @GetMapping("/me/active")
    @PreAuthorize("hasRole('PATRON')")
    @Operation(summary = "Get current user's active borrowings", 
               description = "Retrieves active (not returned) borrowings for the authenticated user (requires PATRON role)")
    @ApiResponse(responseCode = "200", description = "Active borrowings retrieved successfully")
    public ResponseEntity<Page<BorrowingResponseDto>> getMyActiveBorrowings(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("Request for user active borrowings: User ID: {}, Page: {}, Size: {}", 
                userPrincipal.getId(), page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        
        Page<BorrowingResponseDto> activeBorrowings = borrowingService.getUserActiveBorrowings(userPrincipal.getId(), pageable);
        
        logger.info("Retrieved {} active borrowings for user: {} (Page {} of {})", 
                activeBorrowings.getNumberOfElements(), userPrincipal.getId(), 
                activeBorrowings.getNumber(), activeBorrowings.getTotalPages());
        
        return ResponseEntity.ok(activeBorrowings);
    }

    @GetMapping("/me/history")
    @PreAuthorize("hasRole('PATRON')")
    @Operation(summary = "Get current user's borrowing history", 
               description = "Retrieves borrowing history (returned books) for the authenticated user (requires PATRON role)")
    @ApiResponse(responseCode = "200", description = "Borrowing history retrieved successfully")
    public ResponseEntity<Page<BorrowingResponseDto>> getMyBorrowingHistory(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = "returnDate") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "desc") String direction) {
        
        logger.info("Request for user borrowing history: User ID: {}, Page: {}, Size: {}, Sort: {} {}", 
                userPrincipal.getId(), page, size, sortBy, direction);
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<BorrowingResponseDto> borrowingHistory = borrowingService.getUserBorrowingHistory(userPrincipal.getId(), pageable);
        
        logger.info("Retrieved {} borrowing history records for user: {} (Page {} of {})", 
                borrowingHistory.getNumberOfElements(), userPrincipal.getId(), 
                borrowingHistory.getNumber(), borrowingHistory.getTotalPages());
        
        return ResponseEntity.ok(borrowingHistory);
    }

    @GetMapping("/{borrowingId}")
    @PreAuthorize("hasAnyRole('PATRON', 'LIBRARIAN')")
    @Operation(summary = "Get borrowing by ID", 
               description = "Retrieves details of a specific borrowing record (requires PATRON or LIBRARIAN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Borrowing found and returned successfully"),
        @ApiResponse(responseCode = "404", description = "Borrowing record not found")
    })
    public ResponseEntity<BorrowingResponseDto> getBorrowingById(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "ID of the borrowing record to retrieve", required = true)
            @PathVariable Long borrowingId) {
        
        logger.info("Request to get borrowing by ID: {}, Requested by User ID: {}", 
                borrowingId, userPrincipal.getId());
        
        BorrowingResponseDto borrowing = borrowingService.getBorrowingById(borrowingId);
        
        logger.info("Retrieved borrowing: ID: {}, Book: {}, User: {}, Status: {}", 
                borrowing.getId(), borrowing.getBookTitle(), borrowing.getUserName(), borrowing.getStatus());
        
        return ResponseEntity.ok(borrowing);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get borrowings by status", 
               description = "Retrieves borrowings filtered by status (requires LIBRARIAN role)")
    @ApiResponse(responseCode = "200", description = "Borrowings retrieved successfully")
    public ResponseEntity<Page<BorrowingResponseDto>> getBorrowingsByStatus(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Status to filter by (BORROWED, RETURNED, OVERDUE)", required = true)
            @PathVariable BorrowingStatus status,
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("Request for borrowings by status: Status: {}, Page: {}, Size: {}, Requested by User ID: {}", 
                status, page, size, userPrincipal.getId());
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        
        Page<BorrowingResponseDto> borrowings = borrowingService.getBorrowingsByStatus(status, pageable);
        
        logger.info("Retrieved {} borrowings with status {}: (Page {} of {})", 
                borrowings.getNumberOfElements(), status, 
                borrowings.getNumber(), borrowings.getTotalPages());
        
        return ResponseEntity.ok(borrowings);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Page<BorrowingResponseDto>> getUserBorrowings(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("Request for user borrowings by librarian: Target User ID: {}, Page: {}, Size: {}, Requested by User ID: {}", 
                userId, page, size, userPrincipal.getId());
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("borrowDate").descending());
        
        Page<BorrowingResponseDto> borrowings = borrowingService.getUserBorrowings(userId, pageable);
        
        logger.info("Retrieved {} borrowings for user {}: (Page {} of {})", 
                borrowings.getNumberOfElements(), userId, 
                borrowings.getNumber(), borrowings.getTotalPages());
        
        return ResponseEntity.ok(borrowings);
    }

    @GetMapping("/user/{userId}/history")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Page<BorrowingResponseDto>> getUserBorrowingHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "returnDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        logger.info("Request for user borrowing history by librarian: Target User ID: {}, Page: {}, Size: {}, Sort: {} {}, Requested by User ID: {}", 
                userId, page, size, sortBy, direction, userPrincipal.getId());
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<BorrowingResponseDto> borrowingHistory = borrowingService.getUserBorrowingHistory(userId, pageable);
        
        logger.info("Retrieved {} borrowing history records for user {}: (Page {} of {})", 
                borrowingHistory.getNumberOfElements(), userId, 
                borrowingHistory.getNumber(), borrowingHistory.getTotalPages());
        
        return ResponseEntity.ok(borrowingHistory);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Page<BorrowingResponseDto>> getAllBorrowings(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "borrowDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        logger.info("Request for all borrowings: Page: {}, Size: {}, Sort: {} {}, Requested by User ID: {}", 
                page, size, sortBy, direction, userPrincipal.getId());
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<BorrowingResponseDto> allBorrowings = borrowingService.getAllBorrowings(pageable);
        
        logger.info("Retrieved {} total borrowings: (Page {} of {})", 
                allBorrowings.getNumberOfElements(), 
                allBorrowings.getNumber(), allBorrowings.getTotalPages());
        
        return ResponseEntity.ok(allBorrowings);
    }

    @GetMapping("/overdue/report")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get overdue books report", 
               description = "Generates a report of all overdue books (requires LIBRARIAN role)")
    @ApiResponse(responseCode = "200", description = "Overdue books report generated successfully")
    public ResponseEntity<List<BorrowingResponseDto>> getOverdueReport(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Request for overdue borrowings report, Requested by User ID: {}", 
                userPrincipal.getId());
        
        List<BorrowingResponseDto> overdueBooks = borrowingService.getAllOverdueBooks();
        
        logger.info("Retrieved {} overdue borrowings", overdueBooks.size());
        
        return ResponseEntity.ok(overdueBooks);
    }
} 