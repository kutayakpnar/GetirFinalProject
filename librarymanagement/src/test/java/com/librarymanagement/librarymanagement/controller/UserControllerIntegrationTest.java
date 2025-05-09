package com.librarymanagement.librarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarymanagement.librarymanagement.dto.UserRegistrationRequest;
import com.librarymanagement.librarymanagement.dto.UserUpdateRequest;
import com.librarymanagement.librarymanagement.model.Role;
import com.librarymanagement.librarymanagement.model.User;
import com.librarymanagement.librarymanagement.repository.UserRepository;
import com.librarymanagement.librarymanagement.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User patron;
    private User librarian;
    private String patronToken;
    private String librarianToken;

    @BeforeEach
    void setUp() {
        // Clean existing data
        userRepository.deleteAll();

        // Create test users
        patron = userRepository.save(User.builder()
                .email("patron@example.com")
                .password(passwordEncoder.encode("password"))
                .firstName("Test")
                .lastName("Patron")
                .phoneNumber("+1234567890")
                .role(Role.PATRON)
                .address("123 Patron St")
                .createdAt(LocalDateTime.now())
                .build());

        librarian = userRepository.save(User.builder()
                .email("librarian@example.com")
                .password(passwordEncoder.encode("password"))
                .firstName("Test")
                .lastName("Librarian")
                .phoneNumber("+1987654321")
                .role(Role.LIBRARIAN)
                .address("456 Librarian Ave")
                .createdAt(LocalDateTime.now())
                .build());

        // Generate tokens
        patronToken = jwtTokenProvider.generateToken(patron);
        librarianToken = jwtTokenProvider.generateToken(librarian);
    }

    @Test
    void registerUser_Success() throws Exception {
        // Prepare registration request
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .firstName("New")
                .lastName("User")
                .email("newuser@example.com")
                .password("password123")
                .phoneNumber("+1555555555")
                .role(Role.PATRON)
                .address("789 New St")
                .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.role").value(request.getRole().name()));
    }

    @Test
    void registerUser_EmailAlreadyExists() throws Exception {
        // Prepare registration request with existing email
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .firstName("Duplicate")
                .lastName("User")
                .email("patron@example.com")  // This email already exists
                .password("password123")
                .phoneNumber("+1555555555")
                .role(Role.PATRON)
                .address("789 New St")
                .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    void getAllUsers_LibrarianAccess_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    @Test
    void getAllUsers_PatronAccess_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getUserById_LibrarianAccess_Success() throws Exception {
        mockMvc.perform(get("/api/users/{id}", patron.getId())
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patron.getId()))
                .andExpect(jsonPath("$.firstName").value(patron.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(patron.getLastName()))
                .andExpect(jsonPath("$.email").value(patron.getEmail()));
    }

    @Test
    void getUserById_PatronAccess_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users/{id}", librarian.getId())
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getUserById_NotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L)
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_LibrarianAccess_Success() throws Exception {
        // Prepare update request
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Patron")
                .email(patron.getEmail())
                .phoneNumber(patron.getPhoneNumber())
                .role(patron.getRole())
                .address("Updated Address")
                .build();

        mockMvc.perform(put("/api/users/{id}", patron.getId())
                .header("Authorization", "Bearer " + librarianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patron.getId()))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.address").value("Updated Address"));
    }

    @Test
    void updateUser_PatronAccess_Forbidden() throws Exception {
        // Prepare update request
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Patron")
                .build();

        mockMvc.perform(put("/api/users/{id}", patron.getId())
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateUser_EmailAlreadyExists() throws Exception {
        // Prepare update request with email that already exists
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Patron")
                .email("librarian@example.com")  // This email belongs to another user
                .build();

        mockMvc.perform(put("/api/users/{id}", patron.getId())
                .header("Authorization", "Bearer " + librarianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already in use")));
    }

    @Test
    void deleteUser_LibrarianAccess_Success() throws Exception {
        // Create a user to delete
        User userToDelete = userRepository.save(User.builder()
                .email("todelete@example.com")
                .password(passwordEncoder.encode("password"))
                .firstName("To")
                .lastName("Delete")
                .phoneNumber("+1111222333")
                .role(Role.PATRON)
                .address("Delete Address")
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(delete("/api/users/{id}", userToDelete.getId())
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_PatronAccess_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", patron.getId())
                .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteUser_NotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 999L)
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNotFound());
    }
} 