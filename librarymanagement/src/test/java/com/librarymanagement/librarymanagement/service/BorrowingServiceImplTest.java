package com.librarymanagement.librarymanagement.service;

import com.librarymanagement.librarymanagement.dto.BorrowBookRequestDto;
import com.librarymanagement.librarymanagement.dto.BorrowingResponseDto;
import com.librarymanagement.librarymanagement.exception.BookNotFoundException;
import com.librarymanagement.librarymanagement.exception.BookNotAvailableException;
import com.librarymanagement.librarymanagement.exception.BorrowingLimitExceededException;
import com.librarymanagement.librarymanagement.exception.UserNotFoundException;
import com.librarymanagement.librarymanagement.model.*;
import com.librarymanagement.librarymanagement.repository.BookRepository;
import com.librarymanagement.librarymanagement.repository.BorrowingRepository;
import com.librarymanagement.librarymanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceImplTest {

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    private User patron;
    private User librarian;
    private Book book;
    private Borrowing borrowing;

    @BeforeEach
    void setUp() {
        // Set up test data
        patron = User.builder()
                .id(1L)
                .email("patron@example.com")
                .firstName("Test")
                .lastName("Patron")
                .role(Role.PATRON)
                .build();

        librarian = User.builder()
                .id(2L)
                .email("librarian@example.com")
                .firstName("Test")
                .lastName("Librarian")
                .role(Role.LIBRARIAN)
                .build();

        book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .isbn("1234567890")
                .available(true)
                .build();

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);

        borrowing = Borrowing.builder()
                .id(1L)
                .user(patron)
                .book(book)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .status(BorrowingStatus.BORROWED)
                .createdAt(LocalDateTime.now())
                .build();

        // Set the private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(borrowingService, "maxBooksPerUser", 5);
        ReflectionTestUtils.setField(borrowingService, "defaultBorrowingPeriodInDays", 14);
    }

    @Test
    void borrowBook_Success() {
        // Arrange
        BorrowBookRequestDto requestDto = new BorrowBookRequestDto();
        requestDto.setBookId(book.getId());

        when(userRepository.findById(patron.getId())).thenReturn(Optional.of(patron));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(borrowingRepository.countByUserAndStatusIn(any(), anyList())).thenReturn(0);
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(borrowing);

        // Act
        BorrowingResponseDto result = borrowingService.borrowBook(patron.getId(), requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(book.getId(), result.getBookId());
        assertEquals(patron.getId(), result.getUserId());
        assertEquals(BorrowingStatus.BORROWED, result.getStatus());
        verify(bookRepository).save(any(Book.class));
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    void borrowBook_UserNotFound() {
        // Arrange
        BorrowBookRequestDto requestDto = new BorrowBookRequestDto();
        requestDto.setBookId(book.getId());

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            borrowingService.borrowBook(99L, requestDto);
        });
    }

    @Test
    void borrowBook_BookNotFound() {
        // Arrange
        BorrowBookRequestDto requestDto = new BorrowBookRequestDto();
        requestDto.setBookId(99L);

        when(userRepository.findById(patron.getId())).thenReturn(Optional.of(patron));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> {
            borrowingService.borrowBook(patron.getId(), requestDto);
        });
    }

    @Test
    void borrowBook_BookNotAvailable() {
        // Arrange
        BorrowBookRequestDto requestDto = new BorrowBookRequestDto();
        requestDto.setBookId(book.getId());
        
        book.setAvailable(false);
        
        Borrowing activeBorrowing = Borrowing.builder()
                .book(book)
                .user(patron)
                .status(BorrowingStatus.BORROWED)
                .build();

        when(userRepository.findById(patron.getId())).thenReturn(Optional.of(patron));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(borrowingRepository.findByBookIdAndStatusNot(book.getId(), BorrowingStatus.RETURNED))
                .thenReturn(Optional.of(activeBorrowing));

        // Act & Assert
        assertThrows(BookNotAvailableException.class, () -> {
            borrowingService.borrowBook(patron.getId(), requestDto);
        });
    }

    @Test
    void borrowBook_BorrowingLimitExceeded() {
        // Arrange
        BorrowBookRequestDto requestDto = new BorrowBookRequestDto();
        requestDto.setBookId(book.getId());

        when(userRepository.findById(patron.getId())).thenReturn(Optional.of(patron));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(borrowingRepository.countByUserAndStatusIn(any(), anyList())).thenReturn(5);

        // Act & Assert
        assertThrows(BorrowingLimitExceededException.class, () -> {
            borrowingService.borrowBook(patron.getId(), requestDto);
        });
    }

    @Test
    void returnBook_Success() {
        // Arrange
        when(userRepository.findById(patron.getId())).thenReturn(Optional.of(patron));
        when(borrowingRepository.findById(borrowing.getId())).thenReturn(Optional.of(borrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(borrowing);

        // Act
        BorrowingResponseDto result = borrowingService.returnBook(patron.getId(), borrowing.getId());

        // Assert
        assertNotNull(result);
        assertEquals(BorrowingStatus.RETURNED, borrowing.getStatus());
        assertNotNull(borrowing.getReturnDate());
        assertTrue(book.getAvailable());
        verify(bookRepository).save(book);
    }

    @Test
    void getUserBorrowingHistory_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Borrowing> borrowingPage = new PageImpl<>(List.of(borrowing));

        when(userRepository.findById(patron.getId())).thenReturn(Optional.of(patron));
        when(borrowingRepository.findByUserAndStatus(patron, BorrowingStatus.RETURNED, pageable))
                .thenReturn(borrowingPage);

        // Act
        Page<BorrowingResponseDto> result = borrowingService.getUserBorrowingHistory(patron.getId(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(borrowing.getId(), result.getContent().get(0).getId());
    }

    @Test
    void getAllOverdueBooks_Success() {
        // Arrange
        borrowing.setStatus(BorrowingStatus.OVERDUE);
        List<Borrowing> overdueList = List.of(borrowing);

        when(borrowingRepository.findByStatus(BorrowingStatus.OVERDUE))
                .thenReturn(overdueList);

        // Act
        List<BorrowingResponseDto> result = borrowingService.getAllOverdueBooks();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BorrowingStatus.OVERDUE, result.get(0).getStatus());
    }

    @Test
    void getAllBorrowings_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Borrowing> borrowingPage = new PageImpl<>(List.of(borrowing));

        when(borrowingRepository.findAll(pageable)).thenReturn(borrowingPage);

        // Act
        Page<BorrowingResponseDto> result = borrowingService.getAllBorrowings(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void checkAndUpdateOverdueBooks_Success() {
        // Arrange
        List<Borrowing> overdueBorrowings = List.of(borrowing);
        when(borrowingRepository.findOverdueBooks(any(LocalDate.class)))
                .thenReturn(overdueBorrowings);

        // Act
        borrowingService.checkAndUpdateOverdueBooks();

        // Assert
        assertEquals(BorrowingStatus.OVERDUE, borrowing.getStatus());
        verify(borrowingRepository).save(borrowing);
    }
} 