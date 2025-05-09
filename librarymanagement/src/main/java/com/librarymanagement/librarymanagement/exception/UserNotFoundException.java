package com.librarymanagement.librarymanagement.exception;
 
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
} 