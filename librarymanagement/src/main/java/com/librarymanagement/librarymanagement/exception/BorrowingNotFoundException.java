package com.librarymanagement.librarymanagement.exception;

public class BorrowingNotFoundException extends RuntimeException {
    public BorrowingNotFoundException(String message) {
        super(message);
    }
} 