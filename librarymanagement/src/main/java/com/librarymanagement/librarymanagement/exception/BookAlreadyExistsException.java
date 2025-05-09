package com.librarymanagement.librarymanagement.exception;
 
public class BookAlreadyExistsException extends RuntimeException {
    public BookAlreadyExistsException(String message) {
        super(message);
    }
} 