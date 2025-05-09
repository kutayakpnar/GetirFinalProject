package com.librarymanagement.librarymanagement.service;

import com.librarymanagement.librarymanagement.dto.BookRequestDto;
import com.librarymanagement.librarymanagement.dto.BookResponseDto;
import com.librarymanagement.librarymanagement.exception.BookAlreadyExistsException;
import com.librarymanagement.librarymanagement.exception.BookNotFoundException;
import com.librarymanagement.librarymanagement.model.Book;
import com.librarymanagement.librarymanagement.model.Genre;
import com.librarymanagement.librarymanagement.repository.BookRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book1;
    private Book book2;
    private BookRequestDto bookRequestDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        LocalDate publicationDate = LocalDate.of(2020, 1, 1);
        LocalDateTime now = LocalDateTime.now();

        book1 = Book.builder()
                .id(1L)
                .title("Test Book 1")
                .author("Test Author 1")
                .isbn("1234567890123")
                .publicationDate(publicationDate)
                .genre(Genre.FICTION)
                .description("Test description 1")
                .publisher("Test Publisher 1")
                .pageCount(200)
                .createdAt(now)
                .available(true)
                .build();

        book2 = Book.builder()
                .id(2L)
                .title("Test Book 2")
                .author("Test Author 2")
                .isbn("9876543210987")
                .publicationDate(publicationDate.plusYears(1))
                .genre(Genre.MYSTERY)
                .description("Test description 2")
                .publisher("Test Publisher 2")
                .pageCount(300)
                .createdAt(now)
                .available(true)
                .build();

        bookRequestDto = BookRequestDto.builder()
                .title("New Book")
                .author("New Author")
                .isbn("5555555555555")
                .publicationDate(publicationDate.plusYears(2))
                .genre(Genre.SCIENCE_FICTION)
                .description("New description")
                .publisher("New Publisher")
                .pageCount(250)
                .build();
    }

    @Test
    void addBook_Success() {
        // Arrange
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(3L);
            return book;
        });

        // Act
        BookResponseDto result = bookService.addBook(bookRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals(bookRequestDto.getTitle(), result.getTitle());
        assertEquals(bookRequestDto.getAuthor(), result.getAuthor());
        assertEquals(bookRequestDto.getIsbn(), result.getIsbn());
        verify(bookRepository).existsByIsbn(bookRequestDto.getIsbn());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBook_IsbnAlreadyExists() {
        // Arrange
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(BookAlreadyExistsException.class, () -> {
            bookService.addBook(bookRequestDto);
        });
        verify(bookRepository).existsByIsbn(bookRequestDto.getIsbn());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void getBookById_Success() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book1));

        // Act
        BookResponseDto result = bookService.getBookById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(book1.getId(), result.getId());
        assertEquals(book1.getTitle(), result.getTitle());
        verify(bookRepository).findById(1L);
    }

    @Test
    void getBookById_NotFound() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> {
            bookService.getBookById(999L);
        });
        verify(bookRepository).findById(999L);
    }

    @Test
    void getBookByIsbn_Success() {
        // Arrange
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book1));

        // Act
        BookResponseDto result = bookService.getBookByIsbn(book1.getIsbn());

        // Assert
        assertNotNull(result);
        assertEquals(book1.getId(), result.getId());
        assertEquals(book1.getIsbn(), result.getIsbn());
        verify(bookRepository).findByIsbn(book1.getIsbn());
    }

    @Test
    void getBookByIsbn_NotFound() {
        // Arrange
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> {
            bookService.getBookByIsbn("nonexistent-isbn");
        });
        verify(bookRepository).findByIsbn("nonexistent-isbn");
    }

    @Test
    void getAllBooks_Success() {
        // Arrange
        List<Book> books = Arrays.asList(book1, book2);
        when(bookRepository.findAll()).thenReturn(books);

        // Act
        List<BookResponseDto> result = bookService.getAllBooks();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(book1.getId(), result.get(0).getId());
        assertEquals(book2.getId(), result.get(1).getId());
        verify(bookRepository).findAll();
    }

    @Test
    void getBooksByTitle_Success() {
        // Arrange
        List<Book> books = Arrays.asList(book1);
        when(bookRepository.findByTitleContainingIgnoreCase(anyString())).thenReturn(books);

        // Act
        List<BookResponseDto> result = bookService.getBooksByTitle("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(book1.getId(), result.get(0).getId());
        verify(bookRepository).findByTitleContainingIgnoreCase("Test");
    }

    @Test
    void getBooksByAuthor_Success() {
        // Arrange
        List<Book> books = Arrays.asList(book1);
        when(bookRepository.findByAuthorContainingIgnoreCase(anyString())).thenReturn(books);

        // Act
        List<BookResponseDto> result = bookService.getBooksByAuthor("Author");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(book1.getId(), result.get(0).getId());
        verify(bookRepository).findByAuthorContainingIgnoreCase("Author");
    }

    @Test
    void getBooksByGenre_Success() {
        // Arrange
        List<Book> books = Arrays.asList(book1);
        when(bookRepository.findByGenre(any(Genre.class))).thenReturn(books);

        // Act
        List<BookResponseDto> result = bookService.getBooksByGenre(Genre.FICTION);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(book1.getId(), result.get(0).getId());
        verify(bookRepository).findByGenre(Genre.FICTION);
    }

    @Test
    void updateBook_Success() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenReturn(book1);

        // Act
        BookResponseDto result = bookService.updateBook(1L, bookRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(book1.getId(), result.getId());
        assertEquals(bookRequestDto.getTitle(), result.getTitle());
        assertEquals(bookRequestDto.getAuthor(), result.getAuthor());
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_NotFound() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> {
            bookService.updateBook(999L, bookRequestDto);
        });
        verify(bookRepository).findById(999L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_IsbnAlreadyExists() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book1));
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        // Set different ISBN from existing book
        bookRequestDto.setIsbn("different-isbn");

        // Act & Assert
        assertThrows(BookAlreadyExistsException.class, () -> {
            bookService.updateBook(1L, bookRequestDto);
        });
        verify(bookRepository).findById(1L);
        verify(bookRepository).existsByIsbn(bookRequestDto.getIsbn());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_Success() {
        // Arrange
        when(bookRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(bookRepository).deleteById(anyLong());

        // Act
        bookService.deleteBook(1L);

        // Assert
        verify(bookRepository).existsById(1L);
        verify(bookRepository).deleteById(1L);
    }

    @Test
    void deleteBook_NotFound() {
        // Arrange
        when(bookRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> {
            bookService.deleteBook(999L);
        });
        verify(bookRepository).existsById(999L);
        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllBooks_Paged_Success() {
        // Arrange
        List<Book> books = Arrays.asList(book1, book2);
        Page<Book> pagedBooks = new PageImpl<>(books);
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(pagedBooks);

        // Act
        Page<BookResponseDto> result = bookService.getAllBooks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(book1.getId(), result.getContent().get(0).getId());
        assertEquals(book2.getId(), result.getContent().get(1).getId());
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void getBooksByTitle_Paged_Success() {
        // Arrange
        List<Book> books = Arrays.asList(book1);
        Page<Book> pagedBooks = new PageImpl<>(books);
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(pagedBooks);

        // Act
        Page<BookResponseDto> result = bookService.getBooksByTitle("Test", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(book1.getId(), result.getContent().get(0).getId());
        verify(bookRepository).findByTitleContainingIgnoreCase("Test", pageable);
    }

    @Test
    void getBooksByAuthor_Paged_Success() {
        // Arrange
        List<Book> books = Arrays.asList(book1);
        Page<Book> pagedBooks = new PageImpl<>(books);
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findByAuthorContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(pagedBooks);

        // Act
        Page<BookResponseDto> result = bookService.getBooksByAuthor("Author", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(book1.getId(), result.getContent().get(0).getId());
        verify(bookRepository).findByAuthorContainingIgnoreCase("Author", pageable);
    }

    @Test
    void getBooksByGenre_Paged_Success() {
        // Arrange
        List<Book> books = Arrays.asList(book1);
        Page<Book> pagedBooks = new PageImpl<>(books);
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findByGenre(any(Genre.class), any(Pageable.class))).thenReturn(pagedBooks);

        // Act
        Page<BookResponseDto> result = bookService.getBooksByGenre(Genre.FICTION, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(book1.getId(), result.getContent().get(0).getId());
        verify(bookRepository).findByGenre(Genre.FICTION, pageable);
    }

    @Test
    void getBooksByIsbn_Paged_Success() {
        // Arrange
        List<Book> books = Arrays.asList(book1);
        Page<Book> pagedBooks = new PageImpl<>(books);
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findByIsbnContaining(anyString(), any(Pageable.class))).thenReturn(pagedBooks);

        // Act
        Page<BookResponseDto> result = bookService.getBooksByIsbn("1234", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(book1.getId(), result.getContent().get(0).getId());
        verify(bookRepository).findByIsbnContaining("1234", pageable);
    }
} 