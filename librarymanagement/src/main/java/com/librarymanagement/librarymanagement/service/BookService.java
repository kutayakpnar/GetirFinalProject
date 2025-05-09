package com.librarymanagement.librarymanagement.service;

import com.librarymanagement.librarymanagement.dto.BookRequestDto;
import com.librarymanagement.librarymanagement.dto.BookResponseDto;
import com.librarymanagement.librarymanagement.model.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    // Non-paginated methods
    BookResponseDto addBook(BookRequestDto bookRequestDto);
    BookResponseDto getBookById(Long id);
    BookResponseDto getBookByIsbn(String isbn);
    List<BookResponseDto> getAllBooks();
    List<BookResponseDto> getBooksByTitle(String title);
    List<BookResponseDto> getBooksByAuthor(String author);
    List<BookResponseDto> getBooksByGenre(Genre genre);
    BookResponseDto updateBook(Long id, BookRequestDto bookRequestDto);
    void deleteBook(Long id);
    
    // Paginated methods
    Page<BookResponseDto> getAllBooks(Pageable pageable);
    Page<BookResponseDto> getBooksByTitle(String title, Pageable pageable);
    Page<BookResponseDto> getBooksByAuthor(String author, Pageable pageable);
    Page<BookResponseDto> getBooksByGenre(Genre genre, Pageable pageable);
    Page<BookResponseDto> getBooksByIsbn(String isbn, Pageable pageable);
} 