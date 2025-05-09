package com.librarymanagement.librarymanagement.service;

import com.librarymanagement.librarymanagement.dto.UserRegistrationRequest;
import com.librarymanagement.librarymanagement.dto.UserResponse;
import com.librarymanagement.librarymanagement.dto.UserUpdateRequest;
import com.librarymanagement.librarymanagement.model.User;

import java.util.List;

public interface UserService {
    UserResponse registerUser(UserRegistrationRequest registrationRequest);
    User registerUserAndReturn(UserRegistrationRequest registrationRequest);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest updateRequest);
    void deleteUser(Long id);
} 