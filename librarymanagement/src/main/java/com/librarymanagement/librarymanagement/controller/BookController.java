package com.librarymanagement.librarymanagement.controller;

import com.librarymanagement.librarymanagement.dto.BookRequestDto;
import com.librarymanagement.librarymanagement.dto.BookResponseDto;
import com.librarymanagement.librarymanagement.model.Genre;
import com.librarymanagement.librarymanagement.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    
    private final BookService bookService;

    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<BookResponseDto> addBook(@Valid @RequestBody BookRequestDto bookRequestDto) {
        logger.info("Request received to add new book with ISBN: {}", bookRequestDto.getIsbn());
        BookResponseDto createdBook = bookService.addBook(bookRequestDto);
        logger.info("Book added successfully with ID: {}", createdBook.getId());
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable Long id) {
        logger.info("Request received to get book by ID: {}", id);
        BookResponseDto book = bookService.getBookById(id);
        logger.info("Book found with ID: {}", id);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookResponseDto> getBookByIsbn(@PathVariable String isbn) {
        logger.info("Request received to get book by ISBN: {}", isbn);
        BookResponseDto book = bookService.getBookByIsbn(isbn);
        logger.info("Book found with ISBN: {}", isbn);
        return ResponseEntity.ok(book);
    }

    @GetMapping
    public ResponseEntity<List<BookResponseDto>> getAllBooks() {
        logger.info("Request received to get all books");
        List<BookResponseDto> books = bookService.getAllBooks();
        logger.info("Returning {} books", books.size());
        return ResponseEntity.ok(books);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<BookResponseDto>> getAllBooksPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        logger.info("Request received for paged books: page={}, size={}, sortBy={}, direction={}", 
                 page, size, sortBy, direction);
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookResponseDto> books = bookService.getAllBooks(pageable);
        
        logger.info("Returning page {} of {} with {} books (total: {})", 
                 books.getNumber(), books.getTotalPages(), books.getNumberOfElements(), books.getTotalElements());
        
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<BookResponseDto>> getBooksByTitle(@RequestParam String title) {
        logger.info("Request received to search books by title: {}", title);
        List<BookResponseDto> books = bookService.getBooksByTitle(title);
        logger.info("Found {} books with title containing: {}", books.size(), title);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/search/title/paged")
    public ResponseEntity<Page<BookResponseDto>> getBooksByTitlePaged(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        logger.info("Request received for paged books by title: title={}, page={}, size={}", 
                title, page, size);
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookResponseDto> books = bookService.getBooksByTitle(title, pageable);
        
        logger.info("Returning page {} of {} with {} books for title search: {}", 
                books.getNumber(), books.getTotalPages(), books.getNumberOfElements(), title);
        
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/author")
    public ResponseEntity<List<BookResponseDto>> getBooksByAuthor(@RequestParam String author) {
        logger.info("Request received to search books by author: {}", author);
        List<BookResponseDto> books = bookService.getBooksByAuthor(author);
        logger.info("Found {} books with author containing: {}", books.size(), author);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/search/author/paged")
    public ResponseEntity<Page<BookResponseDto>> getBooksByAuthorPaged(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "author") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        logger.info("Request received for paged books by author: author={}, page={}, size={}", 
                author, page, size);
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookResponseDto> books = bookService.getBooksByAuthor(author, pageable);
        
        logger.info("Returning page {} of {} with {} books for author search: {}", 
                books.getNumber(), books.getTotalPages(), books.getNumberOfElements(), author);
        
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/genre")
    public ResponseEntity<List<BookResponseDto>> getBooksByGenre(@RequestParam Genre genre) {
        logger.info("Request received to search books by genre: {}", genre);
        List<BookResponseDto> books = bookService.getBooksByGenre(genre);
        logger.info("Found {} books with genre: {}", books.size(), genre);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/search/genre/paged")
    public ResponseEntity<Page<BookResponseDto>> getBooksByGenrePaged(
            @RequestParam Genre genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        logger.info("Request received for paged books by genre: genre={}, page={}, size={}", 
                genre, page, size);
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookResponseDto> books = bookService.getBooksByGenre(genre, pageable);
        
        logger.info("Returning page {} of {} with {} books for genre search: {}", 
                books.getNumber(), books.getTotalPages(), books.getNumberOfElements(), genre);
        
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/search/isbn/paged")
    public ResponseEntity<Page<BookResponseDto>> getBooksByIsbnPaged(
            @RequestParam String isbn,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "isbn") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        logger.info("Request received for paged books by ISBN: isbn={}, page={}, size={}", 
                isbn, page, size);
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookResponseDto> books = bookService.getBooksByIsbn(isbn, pageable);
        
        logger.info("Returning page {} of {} with {} books for ISBN search: {}", 
                books.getNumber(), books.getTotalPages(), books.getNumberOfElements(), isbn);
        
        return ResponseEntity.ok(books);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<BookResponseDto> updateBook(
            @PathVariable Long id, 
            @Valid @RequestBody BookRequestDto bookRequestDto) {
        logger.info("Request received to update book with ID: {}", id);
        BookResponseDto updatedBook = bookService.updateBook(id, bookRequestDto);
        logger.info("Book updated successfully with ID: {}", id);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        logger.info("Request received to delete book with ID: {}", id);
        bookService.deleteBook(id);
        logger.info("Book deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
} 