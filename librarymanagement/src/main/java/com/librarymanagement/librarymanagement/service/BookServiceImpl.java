package com.librarymanagement.librarymanagement.service;

import com.librarymanagement.librarymanagement.dto.BookRequestDto;
import com.librarymanagement.librarymanagement.dto.BookResponseDto;
import com.librarymanagement.librarymanagement.exception.BookAlreadyExistsException;
import com.librarymanagement.librarymanagement.exception.BookNotFoundException;
import com.librarymanagement.librarymanagement.model.Book;
import com.librarymanagement.librarymanagement.model.Genre;
import com.librarymanagement.librarymanagement.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);
    
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public BookResponseDto addBook(BookRequestDto bookRequestDto) {
        logger.debug("Adding new book with ISBN: {}", bookRequestDto.getIsbn());
        
        // Check if book with same ISBN already exists
        if (bookRepository.existsByIsbn(bookRequestDto.getIsbn())) {
            logger.warn("Attempted to add book with existing ISBN: {}", bookRequestDto.getIsbn());
            throw new BookAlreadyExistsException("Book with ISBN " + bookRequestDto.getIsbn() + " already exists");
        }

        // Create and save book
        Book book = Book.builder()
                .title(bookRequestDto.getTitle())
                .author(bookRequestDto.getAuthor())
                .isbn(bookRequestDto.getIsbn())
                .publicationDate(bookRequestDto.getPublicationDate())
                .genre(bookRequestDto.getGenre())
                .description(bookRequestDto.getDescription())
                .publisher(bookRequestDto.getPublisher())
                .pageCount(bookRequestDto.getPageCount())
                .createdAt(LocalDateTime.now())
                .available(true)
                .build();

        Book savedBook = bookRepository.save(book);
        logger.debug("Successfully added new book: ID={}, ISBN={}, Title={}", 
                savedBook.getId(), savedBook.getIsbn(), savedBook.getTitle());
        
        return mapToBookResponseDto(savedBook);
    }

    @Override
    public BookResponseDto getBookById(Long id) {
        logger.debug("Retrieving book by ID: {}", id);
        
        try {
            Book book = bookRepository.findById(id)
                    .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
            
            logger.debug("Found book by ID: {}, Title: {}", id, book.getTitle());
            return mapToBookResponseDto(book);
        } catch (BookNotFoundException e) {
            logger.warn("Book not found with ID: {}", id);
            throw e;
        }
    }

    @Override
    public BookResponseDto getBookByIsbn(String isbn) {
        logger.debug("Retrieving book by ISBN: {}", isbn);
        
        try {
            Book book = bookRepository.findByIsbn(isbn)
                    .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + isbn));
            
            logger.debug("Found book by ISBN: {}, Title: {}", isbn, book.getTitle());
            return mapToBookResponseDto(book);
        } catch (BookNotFoundException e) {
            logger.warn("Book not found with ISBN: {}", isbn);
            throw e;
        }
    }

    @Override
    public List<BookResponseDto> getAllBooks() {
        logger.debug("Retrieving all books");
        
        List<Book> books = bookRepository.findAll();
        logger.debug("Retrieved {} books", books.size());
        
        return books.stream()
                .map(this::mapToBookResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponseDto> getBooksByTitle(String title) {
        logger.debug("Searching books by title: {}", title);
        
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase(title);
        logger.debug("Found {} books with title containing: {}", books.size(), title);
        
        return books.stream()
                .map(this::mapToBookResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponseDto> getBooksByAuthor(String author) {
        logger.debug("Searching books by author: {}", author);
        
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase(author);
        logger.debug("Found {} books with author containing: {}", books.size(), author);
        
        return books.stream()
                .map(this::mapToBookResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponseDto> getBooksByGenre(Genre genre) {
        logger.debug("Searching books by genre: {}", genre);
        
        List<Book> books = bookRepository.findByGenre(genre);
        logger.debug("Found {} books with genre: {}", books.size(), genre);
        
        return books.stream()
                .map(this::mapToBookResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookResponseDto updateBook(Long id, BookRequestDto bookRequestDto) {
        logger.debug("Updating book with ID: {}", id);
        
        try {
            Book book = bookRepository.findById(id)
                    .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

            // Check if ISBN is being changed and if the new ISBN already exists
            if (!book.getIsbn().equals(bookRequestDto.getIsbn()) && 
                    bookRepository.existsByIsbn(bookRequestDto.getIsbn())) {
                logger.warn("Cannot update book ID: {} - new ISBN {} already exists", 
                        id, bookRequestDto.getIsbn());
                throw new BookAlreadyExistsException("Book with ISBN " + bookRequestDto.getIsbn() + " already exists");
            }

            // Update book
            book.setTitle(bookRequestDto.getTitle());
            book.setAuthor(bookRequestDto.getAuthor());
            book.setIsbn(bookRequestDto.getIsbn());
            book.setPublicationDate(bookRequestDto.getPublicationDate());
            book.setGenre(bookRequestDto.getGenre());
            book.setDescription(bookRequestDto.getDescription());
            book.setPublisher(bookRequestDto.getPublisher());
            book.setPageCount(bookRequestDto.getPageCount());
            book.setUpdatedAt(LocalDateTime.now());

            Book updatedBook = bookRepository.save(book);
            logger.debug("Successfully updated book: ID={}, ISBN={}, Title={}", 
                    updatedBook.getId(), updatedBook.getIsbn(), updatedBook.getTitle());
            
            return mapToBookResponseDto(updatedBook);
        } catch (BookNotFoundException e) {
            logger.warn("Failed to update - Book not found with ID: {}", id);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        logger.debug("Deleting book with ID: {}", id);
        
        if (!bookRepository.existsById(id)) {
            logger.warn("Failed to delete - Book not found with ID: {}", id);
            throw new BookNotFoundException("Book not found with id: " + id);
        }
        
        bookRepository.deleteById(id);
        logger.debug("Successfully deleted book with ID: {}", id);
    }
    
    // Paginated methods
    @Override
    public Page<BookResponseDto> getAllBooks(Pageable pageable) {
        logger.debug("Retrieving paged books: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Book> bookPage = bookRepository.findAll(pageable);
        logger.debug("Retrieved page {} of {} with {} books", 
                bookPage.getNumber(), bookPage.getTotalPages(), bookPage.getNumberOfElements());
        
        return bookPage.map(this::mapToBookResponseDto);
    }

    @Override
    public Page<BookResponseDto> getBooksByTitle(String title, Pageable pageable) {
        logger.debug("Searching books by title with pagination: title={}, page={}, size={}", 
                title, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Book> bookPage = bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        logger.debug("Found page {} of {} with {} books for title search: {}", 
                bookPage.getNumber(), bookPage.getTotalPages(), bookPage.getNumberOfElements(), title);
        
        return bookPage.map(this::mapToBookResponseDto);
    }

    @Override
    public Page<BookResponseDto> getBooksByAuthor(String author, Pageable pageable) {
        logger.debug("Searching books by author with pagination: author={}, page={}, size={}", 
                author, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Book> bookPage = bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
        logger.debug("Found page {} of {} with {} books for author search: {}", 
                bookPage.getNumber(), bookPage.getTotalPages(), bookPage.getNumberOfElements(), author);
        
        return bookPage.map(this::mapToBookResponseDto);
    }

    @Override
    public Page<BookResponseDto> getBooksByGenre(Genre genre, Pageable pageable) {
        logger.debug("Searching books by genre with pagination: genre={}, page={}, size={}", 
                genre, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Book> bookPage = bookRepository.findByGenre(genre, pageable);
        logger.debug("Found page {} of {} with {} books for genre search: {}", 
                bookPage.getNumber(), bookPage.getTotalPages(), bookPage.getNumberOfElements(), genre);
        
        return bookPage.map(this::mapToBookResponseDto);
    }

    @Override
    public Page<BookResponseDto> getBooksByIsbn(String isbn, Pageable pageable) {
        logger.debug("Searching books by ISBN with pagination: isbn={}, page={}, size={}", 
                isbn, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Book> bookPage = bookRepository.findByIsbnContaining(isbn, pageable);
        logger.debug("Found page {} of {} with {} books for ISBN search: {}", 
                bookPage.getNumber(), bookPage.getTotalPages(), bookPage.getNumberOfElements(), isbn);
        
        return bookPage.map(this::mapToBookResponseDto);
    }

    private BookResponseDto mapToBookResponseDto(Book book) {
        return BookResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publicationDate(book.getPublicationDate())
                .genre(book.getGenre())
                .description(book.getDescription())
                .publisher(book.getPublisher())
                .pageCount(book.getPageCount())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .available(book.getAvailable())
                .build();
    }
} 