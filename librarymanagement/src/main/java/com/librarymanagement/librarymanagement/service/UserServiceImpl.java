package com.librarymanagement.librarymanagement.service;

import com.librarymanagement.librarymanagement.dto.UserRegistrationRequest;
import com.librarymanagement.librarymanagement.dto.UserResponse;
import com.librarymanagement.librarymanagement.dto.UserUpdateRequest;
import com.librarymanagement.librarymanagement.exception.UserAlreadyExistsException;
import com.librarymanagement.librarymanagement.exception.UserNotFoundException;
import com.librarymanagement.librarymanagement.model.User;
import com.librarymanagement.librarymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse registerUser(UserRegistrationRequest request) {
        logger.debug("Processing user registration request for email: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed - user with email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user entity
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .address(request.getAddress())
                .createdAt(LocalDateTime.now())
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);
        logger.info("User successfully registered with ID: {}, email: {}, role: {}", 
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        // Map to response DTO
        return mapUserToUserResponse(savedUser);
    }
    
    @Override
    public User registerUserAndReturn(UserRegistrationRequest request) {
        logger.debug("Processing user registration and returning entity for email: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed - user with email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user entity
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .address(request.getAddress())
                .createdAt(LocalDateTime.now())
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);
        logger.info("User successfully registered and returned with ID: {}, email: {}, role: {}", 
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        
        return savedUser;
    }
    
    @Override
    public List<UserResponse> getAllUsers() {
        logger.debug("Retrieving all users");
        
        List<User> users = userRepository.findAll();
        logger.debug("Retrieved {} users from database", users.size());
        
        return users.stream()
                .map(this::mapUserToUserResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserResponse getUserById(Long id) {
        logger.debug("Retrieving user by ID: {}", id);
        
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
            
            logger.debug("Found user by ID: {}, email: {}", id, user.getEmail());
            return mapUserToUserResponse(user);
        } catch (UserNotFoundException e) {
            logger.warn("User not found with ID: {}", id);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest updateRequest) {
        logger.debug("Updating user with ID: {}", id);
        
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
            
            // Check if email already exists for another user
            if (updateRequest.getEmail() != null && 
                    !updateRequest.getEmail().equals(user.getEmail()) && 
                    userRepository.existsByEmail(updateRequest.getEmail())) {
                logger.warn("Update failed - email {} is already in use by another user", updateRequest.getEmail());
                throw new UserAlreadyExistsException("Email is already in use: " + updateRequest.getEmail());
            }
            
            // Log fields that are being updated
            logger.debug("Updating user ID: {} - Fields to update: {}", id, 
                    getFieldsToUpdate(updateRequest, user));
            
            // Update fields if they are provided in the request
            if (updateRequest.getFirstName() != null) {
                user.setFirstName(updateRequest.getFirstName());
            }
            
            if (updateRequest.getLastName() != null) {
                user.setLastName(updateRequest.getLastName());
            }
            
            if (updateRequest.getEmail() != null) {
                user.setEmail(updateRequest.getEmail());
            }
            
            if (updateRequest.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
                logger.debug("Password updated for user ID: {}", id);
            }
            
            if (updateRequest.getPhoneNumber() != null) {
                user.setPhoneNumber(updateRequest.getPhoneNumber());
            }
            
            if (updateRequest.getRole() != null) {
                user.setRole(updateRequest.getRole());
            }
            
            if (updateRequest.getAddress() != null) {
                user.setAddress(updateRequest.getAddress());
            }
            
            user.setUpdatedAt(LocalDateTime.now());
            
            User updatedUser = userRepository.save(user);
            logger.info("User successfully updated with ID: {}", updatedUser.getId());
            
            return mapUserToUserResponse(updatedUser);
        } catch (UserNotFoundException e) {
            logger.warn("Update failed - User not found with ID: {}", id);
            throw e;
        }
    }
    
    @Override
    public void deleteUser(Long id) {
        logger.debug("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            logger.warn("Delete failed - User not found with ID: {}", id);
            throw new UserNotFoundException("User not found with id: " + id);
        }
        
        userRepository.deleteById(id);
        logger.info("User successfully deleted with ID: {}", id);
    }
    
    private UserResponse mapUserToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    /**
     * Helper method to log which fields are being updated
     */
    private String getFieldsToUpdate(UserUpdateRequest request, User currentUser) {
        StringBuilder fieldsToUpdate = new StringBuilder();
        
        if (request.getFirstName() != null && !request.getFirstName().equals(currentUser.getFirstName())) {
            fieldsToUpdate.append("firstName, ");
        }
        
        if (request.getLastName() != null && !request.getLastName().equals(currentUser.getLastName())) {
            fieldsToUpdate.append("lastName, ");
        }
        
        if (request.getEmail() != null && !request.getEmail().equals(currentUser.getEmail())) {
            fieldsToUpdate.append("email, ");
        }
        
        if (request.getPassword() != null) {
            fieldsToUpdate.append("password, ");
        }
        
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
            fieldsToUpdate.append("phoneNumber, ");
        }
        
        if (request.getRole() != null && !request.getRole().equals(currentUser.getRole())) {
            fieldsToUpdate.append("role, ");
        }
        
        if (request.getAddress() != null && !request.getAddress().equals(currentUser.getAddress())) {
            fieldsToUpdate.append("address, ");
        }
        
        if (fieldsToUpdate.length() > 0) {
            fieldsToUpdate.setLength(fieldsToUpdate.length() - 2);  // Remove trailing comma and space
            return fieldsToUpdate.toString();
        } else {
            return "none";
        }
    }
} 