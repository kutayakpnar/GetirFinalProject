package com.librarymanagement.librarymanagement.controller;

import com.librarymanagement.librarymanagement.dto.AuthResponse;
import com.librarymanagement.librarymanagement.dto.UserRegistrationRequest;
import com.librarymanagement.librarymanagement.dto.UserResponse;
import com.librarymanagement.librarymanagement.dto.UserUpdateRequest;
import com.librarymanagement.librarymanagement.model.User;
import com.librarymanagement.librarymanagement.security.JwtTokenProvider;
import com.librarymanagement.librarymanagement.security.UserPrincipal;
import com.librarymanagement.librarymanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users in the library system")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user in the system and returns an authentication token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "User already exists with the provided email")
    })
    public ResponseEntity<AuthResponse> registerUser(
            @Parameter(description = "User registration details", required = true) 
            @Valid @RequestBody UserRegistrationRequest request) {
        logger.info("User registration request received for email: {}", request.getEmail());
        
        User user = userService.registerUserAndReturn(request);
        
        // Generate JWT token
        String jwt = tokenProvider.generateToken(user);
        
        // Create auth response
        AuthResponse authResponse = AuthResponse.builder()
                .token(jwt)
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        
        logger.info("User successfully registered: ID: {}, Email: {}, Role: {}", 
                user.getId(), user.getEmail(), user.getRole());
        
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users in the system (requires LIBRARIAN role)")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.info("Request to get all users received, Requested by User ID: {}", 
                userPrincipal.getId());
        
        List<UserResponse> users = userService.getAllUsers();
        
        logger.info("Retrieved {} users", users.size());
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID (requires LIBRARIAN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found and returned successfully"),
        @ApiResponse(responseCode = "404", description = "User not found with the given ID")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "ID of the user to retrieve", required = true)
            @PathVariable Long id) {
        
        logger.info("Request to get user by ID: {}, Requested by User ID: {}", 
                id, userPrincipal.getId());
        
        UserResponse user = userService.getUserById(id);
        
        logger.info("Retrieved user: ID: {}, Email: {}, Role: {}", 
                user.getId(), user.getEmail(), user.getRole());
        
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Update user", description = "Updates user information by ID (requires LIBRARIAN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "User not found with the given ID"),
        @ApiResponse(responseCode = "409", description = "Email already in use by another user")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "ID of the user to update", required = true)
            @PathVariable Long id, 
            @Parameter(description = "Updated user information", required = true)
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        
        logger.info("Request to update user ID: {}, Requested by User ID: {}", 
                id, userPrincipal.getId());
        
        // Log which fields are being updated
        StringBuilder updateFields = new StringBuilder("Fields being updated: ");
        if (updateRequest.getFirstName() != null) updateFields.append("firstName, ");
        if (updateRequest.getLastName() != null) updateFields.append("lastName, ");
        if (updateRequest.getEmail() != null) updateFields.append("email, ");
        if (updateRequest.getPassword() != null) updateFields.append("password, ");
        if (updateRequest.getPhoneNumber() != null) updateFields.append("phoneNumber, ");
        if (updateRequest.getRole() != null) updateFields.append("role, ");
        if (updateRequest.getAddress() != null) updateFields.append("address, ");
        
        if (updateFields.length() > 21) { // "Fields being updated: " length is 21
            updateFields.setLength(updateFields.length() - 2); // Remove trailing comma and space
            logger.info(updateFields.toString());
        } else {
            logger.info("No fields specified for update");
        }
        
        UserResponse updatedUser = userService.updateUser(id, updateRequest);
        
        logger.info("User successfully updated: ID: {}, Email: {}, Role: {}", 
                updatedUser.getId(), updatedUser.getEmail(), updatedUser.getRole());
        
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Delete user", description = "Deletes a user from the system by ID (requires LIBRARIAN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found with the given ID")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "ID of the user to delete", required = true)
            @PathVariable Long id) {
        
        logger.info("Request to delete user ID: {}, Requested by User ID: {}", 
                id, userPrincipal.getId());
        
        userService.deleteUser(id);
        
        logger.info("User successfully deleted: ID: {}", id);
        
        return ResponseEntity.noContent().build();
    }
} 