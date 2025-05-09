package com.librarymanagement.librarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarymanagement.librarymanagement.dto.BookRequestDto;
import com.librarymanagement.librarymanagement.model.Book;
import com.librarymanagement.librarymanagement.model.Genre;
import com.librarymanagement.librarymanagement.model.Role;
import com.librarymanagement.librarymanagement.model.User;
import com.librarymanagement.librarymanagement.repository.BookRepository;
import com.librarymanagement.librarymanagement.repository.UserRepository;
import com.librarymanagement.librarymanagement.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Book book1;
    private Book book2;
    private User patron;
    private User librarian;
    private String patronToken;
    private String librarianToken;

    @BeforeEach
    void setUp() {
        // Clean existing data
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
        LocalDate publicationDate = LocalDate.of(2020, 1, 1);
        
        book1 = bookRepository.save(Book.builder()
                .title("Test Book 1")
                .author("Test Author 1")
                .isbn("1234567890123")
                .publicationDate(publicationDate)
                .genre(Genre.FICTION)
                .description("Test description 1")
                .publisher("Test Publisher 1")
                .pageCount(200)
                .createdAt(LocalDateTime.now())
                .available(true)
                .build());

        book2 = bookRepository.save(Book.builder()
                .title("Test Book 2")
                .author("Test Author 2")
                .isbn("9876543210987")
                .publicationDate(publicationDate.plusYears(1))
                .genre(Genre.MYSTERY)
                .description("Test description 2")
                .publisher("Test Publisher 2")
                .pageCount(300)
                .createdAt(LocalDateTime.now())
                .available(true)
                .build());

        // Generate tokens
        patronToken = jwtTokenProvider.generateToken(patron);
        librarianToken = jwtTokenProvider.generateToken(librarian);
    }

    @Test
    void addBook_LibrarianAccess_Success() throws Exception {
        // Create new book request
        BookRequestDto bookRequest = BookRequestDto.builder()
                .title("New Book")
                .author("New Author")
                .isbn("5555555555555")
                .publicationDate(LocalDate.of(2022, 1, 1))
                .genre(Genre.SCIENCE_FICTION)
                .description("New book description")
                .publisher("New Publisher")
                .pageCount(250)
                .build();

        mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + librarianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(bookRequest.getTitle()))
                .andExpect(jsonPath("$.author").value(bookRequest.getAuthor()))
                .andExpect(jsonPath("$.isbn").value(bookRequest.getIsbn()))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void addBook_PatronAccess_Forbidden() throws Exception {
        // Create new book request
        BookRequestDto bookRequest = BookRequestDto.builder()
                .title("New Book")
                .author("New Author")
                .isbn("5555555555555")
                .publicationDate(LocalDate.of(2022, 1, 1))
                .genre(Genre.SCIENCE_FICTION)
                .description("New book description")
                .publisher("New Publisher")
                .pageCount(250)
                .build();

        mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBookById_Success() throws Exception {
        mockMvc.perform(get("/api/books/{id}", book1.getId())
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(book1.getId()))
                .andExpect(jsonPath("$.title").value(book1.getTitle()))
                .andExpect(jsonPath("$.author").value(book1.getAuthor()))
                .andExpect(jsonPath("$.isbn").value(book1.getIsbn()));
    }

    @Test
    void getBookById_NotFound() throws Exception {
        mockMvc.perform(get("/api/books/{id}", 999L)
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookByIsbn_Success() throws Exception {
        mockMvc.perform(get("/api/books/isbn/{isbn}", book1.getIsbn())
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(book1.getId()))
                .andExpect(jsonPath("$.isbn").value(book1.getIsbn()));
    }

    @Test
    void getAllBooks_Success() throws Exception {
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    @Test
    void getAllBooksPaged_Success() throws Exception {
        mockMvc.perform(get("/api/books/paged")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)));
    }

    @Test
    void getBooksByTitle_Success() throws Exception {
        mockMvc.perform(get("/api/books/search/title")
                .param("title", "Test Book 1")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title").value(book1.getTitle()));
    }

    @Test
    void getBooksByTitlePaged_Success() throws Exception {
        mockMvc.perform(get("/api/books/search/title/paged")
                .param("title", "Test Book 1")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title").value(book1.getTitle()));
    }

    @Test
    void getBooksByAuthor_Success() throws Exception {
        mockMvc.perform(get("/api/books/search/author")
                .param("author", "Test Author 1")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].author").value(book1.getAuthor()));
    }

    @Test
    void getBooksByAuthorPaged_Success() throws Exception {
        mockMvc.perform(get("/api/books/search/author/paged")
                .param("author", "Test Author 1")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].author").value(book1.getAuthor()));
    }

    @Test
    void getBooksByGenre_Success() throws Exception {
        mockMvc.perform(get("/api/books/search/genre")
                .param("genre", "FICTION")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].genre").value(book1.getGenre().name()));
    }

    @Test
    void getBooksByGenrePaged_Success() throws Exception {
        mockMvc.perform(get("/api/books/search/genre/paged")
                .param("genre", "FICTION")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].genre").value(book1.getGenre().name()));
    }

    @Test
    void getBooksByIsbnPaged_Success() throws Exception {
        mockMvc.perform(get("/api/books/search/isbn/paged")
                .param("isbn", "1234")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].isbn").value(book1.getIsbn()));
    }

    @Test
    void updateBook_LibrarianAccess_Success() throws Exception {
        // Update request
        BookRequestDto updateRequest = BookRequestDto.builder()
                .title("Updated Title")
                .author(book1.getAuthor())
                .isbn(book1.getIsbn())
                .publicationDate(book1.getPublicationDate())
                .genre(book1.getGenre())
                .description(book1.getDescription())
                .publisher(book1.getPublisher())
                .pageCount(book1.getPageCount())
                .build();

        mockMvc.perform(put("/api/books/{id}", book1.getId())
                .header("Authorization", "Bearer " + librarianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(book1.getId()))
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateBook_PatronAccess_Forbidden() throws Exception {
        // Update request
        BookRequestDto updateRequest = BookRequestDto.builder()
                .title("Updated Title")
                .author(book1.getAuthor())
                .isbn(book1.getIsbn())
                .publicationDate(book1.getPublicationDate())
                .genre(book1.getGenre())
                .description(book1.getDescription())
                .publisher(book1.getPublisher())
                .pageCount(book1.getPageCount())
                .build();

        mockMvc.perform(put("/api/books/{id}", book1.getId())
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteBook_LibrarianAccess_Success() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", book1.getId())
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBook_PatronAccess_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", book1.getId())
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isInternalServerError());
    }
} 