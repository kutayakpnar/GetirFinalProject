package com.librarymanagement.librarymanagement.repository;

import com.librarymanagement.librarymanagement.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BorrowingRepositoryTest {

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private User patron1;
    private User patron2;
    private Book book1;
    private Book book2;
    private Borrowing borrowing1;
    private Borrowing borrowing2;
    private Borrowing borrowing3;

    @BeforeEach
    void setUp() {
        // Clean up previous test data
        borrowingRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        patron1 = userRepository.save(User.builder()
                .email("patron1@example.com")
                .password("password")
                .firstName("Test")
                .lastName("Patron1")
                .role(Role.PATRON)
                .address("123 Test Street")
                .phoneNumber("1234567890")
                .createdAt(LocalDateTime.now())
                .build());

        patron2 = userRepository.save(User.builder()
                .email("patron2@example.com")
                .password("password")
                .firstName("Test")
                .lastName("Patron2")
                .role(Role.PATRON)
                .address("456 Patron Road")
                .phoneNumber("0987654321")
                .createdAt(LocalDateTime.now())
                .build());

        // Create test books
        book1 = bookRepository.save(Book.builder()
                .title("Test Book 1")
                .author("Test Author 1")
                .isbn("1234567890")
                .available(true)
                .genre(Genre.FICTION)
                .publicationDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build());

        book2 = bookRepository.save(Book.builder()
                .title("Test Book 2")
                .author("Test Author 2")
                .isbn("0987654321")
                .available(true)
                .genre(Genre.MYSTERY)
                .publicationDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build());

        // Create borrowings
        LocalDate now = LocalDate.now();

        // Borrowing1: Patron1 borrowed Book1, status BORROWED
        borrowing1 = borrowingRepository.save(Borrowing.builder()
                .user(patron1)
                .book(book1)
                .borrowDate(now.minusDays(10))
                .dueDate(now.plusDays(4))
                .status(BorrowingStatus.BORROWED)
                .createdAt(LocalDateTime.now().minusDays(10))
                .build());

        // Borrowing2: Patron1 borrowed and returned Book2
        borrowing2 = borrowingRepository.save(Borrowing.builder()
                .user(patron1)
                .book(book2)
                .borrowDate(now.minusDays(20))
                .dueDate(now.minusDays(6))
                .returnDate(now.minusDays(8))
                .status(BorrowingStatus.RETURNED)
                .createdAt(LocalDateTime.now().minusDays(20))
                .updatedAt(LocalDateTime.now().minusDays(8))
                .build());

        // Borrowing3: Patron2 has an overdue book (Book2 was returned and can be borrowed again)
        borrowing3 = borrowingRepository.save(Borrowing.builder()
                .user(patron2)
                .book(book2)
                .borrowDate(now.minusDays(30))
                .dueDate(now.minusDays(16))
                .status(BorrowingStatus.OVERDUE)
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now().minusDays(15))
                .build());
    }

    @Test
    void findByUserAndStatusIn_Success() {
        // Test finding all active borrowings for patron1
        List<BorrowingStatus> activeStatuses = Arrays.asList(BorrowingStatus.BORROWED, BorrowingStatus.OVERDUE);
        List<Borrowing> activeBorrowings = borrowingRepository.findByUserAndStatusIn(patron1, activeStatuses);

        assertEquals(1, activeBorrowings.size());
        assertEquals(borrowing1.getId(), activeBorrowings.get(0).getId());
    }

    @Test
    void findByUser_Success() {
        // Test finding all borrowings for patron1
        Page<Borrowing> borrowingsPage = borrowingRepository.findByUser(
                patron1, PageRequest.of(0, 10));

        assertEquals(2, borrowingsPage.getTotalElements());
        assertTrue(borrowingsPage.getContent().stream()
                .anyMatch(b -> b.getId().equals(borrowing1.getId())));
        assertTrue(borrowingsPage.getContent().stream()
                .anyMatch(b -> b.getId().equals(borrowing2.getId())));
    }

    @Test
    void findByStatus_Success() {
        // Test finding all BORROWED borrowings
        Page<Borrowing> borrowedPage = borrowingRepository.findByStatus(
                BorrowingStatus.BORROWED, PageRequest.of(0, 10));

        assertEquals(1, borrowedPage.getTotalElements());
        assertEquals(borrowing1.getId(), borrowedPage.getContent().get(0).getId());

        // Test finding all OVERDUE borrowings
        Page<Borrowing> overduePage = borrowingRepository.findByStatus(
                BorrowingStatus.OVERDUE, PageRequest.of(0, 10));

        assertEquals(1, overduePage.getTotalElements());
        assertEquals(borrowing3.getId(), overduePage.getContent().get(0).getId());
    }

    @Test
    void findByUserAndStatus_Success() {
        // Test finding RETURNED borrowings for patron1
        Page<Borrowing> returnedPage = borrowingRepository.findByUserAndStatus(
                patron1, BorrowingStatus.RETURNED, PageRequest.of(0, 10));

        assertEquals(1, returnedPage.getTotalElements());
        assertEquals(borrowing2.getId(), returnedPage.getContent().get(0).getId());
    }

    @Test
    void findByStatus_ListReturn_Success() {
        // Test finding all OVERDUE borrowings as list
        List<Borrowing> overdueList = borrowingRepository.findByStatus(BorrowingStatus.OVERDUE);

        assertEquals(1, overdueList.size());
        assertEquals(borrowing3.getId(), overdueList.get(0).getId());
    }

    @Test
    void findOverdueBooks_Success() {
        // Set up a book that should be detected as overdue
        LocalDate today = LocalDate.now();
        Borrowing shouldBeOverdue = borrowingRepository.save(Borrowing.builder()
                .user(patron2)
                .book(book1)
                .borrowDate(today.minusDays(20))
                .dueDate(today.minusDays(1))  // Due date was yesterday
                .status(BorrowingStatus.BORROWED)
                .createdAt(LocalDateTime.now().minusDays(20))
                .build());

        // Test finding books that are due
        List<Borrowing> overdueBooks = borrowingRepository.findOverdueBooks(today);

        assertEquals(1, overdueBooks.size());
        assertEquals(shouldBeOverdue.getId(), overdueBooks.get(0).getId());
    }

    @Test
    void findByBookIdAndStatusNot_Success() {
        // Test finding active borrowing for book1
        Optional<Borrowing> activeBorrowing = borrowingRepository.findByBookIdAndStatusNot(
                book1.getId(), BorrowingStatus.RETURNED);

        assertTrue(activeBorrowing.isPresent());
        assertEquals(borrowing1.getId(), activeBorrowing.get().getId());

        // No active borrowing for book2 (it's either returned or overdue)
        Optional<Borrowing> noActiveBorrowing = borrowingRepository.findByBookIdAndStatusNot(
                999L, BorrowingStatus.RETURNED);

        assertFalse(noActiveBorrowing.isPresent());
    }

    @Test
    void countByUserAndStatusIn_Success() {
        // Count active borrowings for patron1
        List<BorrowingStatus> activeStatuses = Arrays.asList(BorrowingStatus.BORROWED, BorrowingStatus.OVERDUE);
        int count = borrowingRepository.countByUserAndStatusIn(patron1, activeStatuses);

        assertEquals(1, count);

        // Count active borrowings for patron2
        int count2 = borrowingRepository.countByUserAndStatusIn(patron2, activeStatuses);

        assertEquals(1, count2);
    }
} 