package com.librarymanagement.librarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarymanagement.librarymanagement.dto.BorrowBookRequestDto;
import com.librarymanagement.librarymanagement.model.*;
import com.librarymanagement.librarymanagement.repository.BookRepository;
import com.librarymanagement.librarymanagement.repository.BorrowingRepository;
import com.librarymanagement.librarymanagement.repository.UserRepository;
import com.librarymanagement.librarymanagement.security.JwtTokenProvider;
import com.librarymanagement.librarymanagement.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class BorrowingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User patron;
    private User librarian;
    private Book book1;
    private Book book2;
    private Borrowing borrowing;
    private String patronToken;
    private String librarianToken;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        borrowingRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        patron = userRepository.save(User.builder()
                .email("patron@example.com")
                .password(passwordEncoder.encode("password"))
                .firstName("Test")
                .lastName("Patron")
                .role(Role.PATRON)
                .address("123 Test Street")
                .phoneNumber("1234567890")
                .createdAt(LocalDateTime.now())
                .build());

        librarian = userRepository.save(User.builder()
                .email("librarian@example.com")
                .password(passwordEncoder.encode("password"))
                .firstName("Test")
                .lastName("Librarian")
                .role(Role.LIBRARIAN)
                .address("456 Admin Street")
                .phoneNumber("9876543210")
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

        // Create a test borrowing
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);

        borrowing = borrowingRepository.save(Borrowing.builder()
                .user(patron)
                .book(book1)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .status(BorrowingStatus.BORROWED)
                .createdAt(LocalDateTime.now())
                .build());

        // Book1 is now borrowed
        book1.setAvailable(false);
        bookRepository.save(book1);

        // Generate JWT tokens
        UserPrincipal patronPrincipal = UserPrincipal.create(patron);
        UserPrincipal librarianPrincipal = UserPrincipal.create(librarian);
        
        patronToken = jwtTokenProvider.generateToken(patron);
        librarianToken = jwtTokenProvider.generateToken(librarian);
    }

    @Test
    void borrowBook_Success() throws Exception {
        BorrowBookRequestDto requestDto = new BorrowBookRequestDto();
        requestDto.setBookId(book2.getId());

        mockMvc.perform(post("/api/borrowings/borrow")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(book2.getId()))
                .andExpect(jsonPath("$.userId").value(patron.getId()))
                .andExpect(jsonPath("$.status").value("BORROWED"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void returnBook_Success() throws Exception {
        mockMvc.perform(post("/api/borrowings/" + borrowing.getId() + "/return")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").isNotEmpty())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getMyBorrowings_Success() throws Exception {
        mockMvc.perform(get("/api/borrowings/me")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].bookId").value(book1.getId()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getMyBorrowingHistory_Success() throws Exception {
        // First return the book to create history
        borrowing.setStatus(BorrowingStatus.RETURNED);
        borrowing.setReturnDate(LocalDate.now());
        borrowingRepository.save(borrowing);

        mockMvc.perform(get("/api/borrowings/me/history")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].status").value("RETURNED"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getAllBorrowings_LibrarianAccess() throws Exception {
        mockMvc.perform(get("/api/borrowings/all")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getAllBorrowings_PatronAccessDenied() throws Exception {
        mockMvc.perform(get("/api/borrowings/all")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isInternalServerError())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getUserBorrowingHistory_LibrarianAccess() throws Exception {
        // First return the book to create history
        borrowing.setStatus(BorrowingStatus.RETURNED);
        borrowing.setReturnDate(LocalDate.now());
        borrowingRepository.save(borrowing);

        mockMvc.perform(get("/api/borrowings/user/" + patron.getId() + "/history")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getOverdueReport_LibrarianAccess() throws Exception {
        // Set a borrowing as overdue
        borrowing.setStatus(BorrowingStatus.OVERDUE);
        borrowingRepository.save(borrowing);

        mockMvc.perform(get("/api/borrowings/overdue/report")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }
} 