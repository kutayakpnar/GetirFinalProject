package com.librarymanagement.librarymanagement.service;

import com.librarymanagement.librarymanagement.dto.UserRegistrationRequest;
import com.librarymanagement.librarymanagement.dto.UserResponse;
import com.librarymanagement.librarymanagement.dto.UserUpdateRequest;
import com.librarymanagement.librarymanagement.exception.UserAlreadyExistsException;
import com.librarymanagement.librarymanagement.exception.UserNotFoundException;
import com.librarymanagement.librarymanagement.model.Role;
import com.librarymanagement.librarymanagement.model.User;
import com.librarymanagement.librarymanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User patron;
    private User librarian;
    private UserRegistrationRequest registrationRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        LocalDateTime now = LocalDateTime.now();

        patron = User.builder()
                .id(1L)
                .firstName("Test")
                .lastName("Patron")
                .email("patron@example.com")
                .password("encoded_password")
                .phoneNumber("+1234567890")
                .role(Role.PATRON)
                .address("123 Patron St")
                .createdAt(now)
                .build();

        librarian = User.builder()
                .id(2L)
                .firstName("Test")
                .lastName("Librarian")
                .email("librarian@example.com")
                .password("encoded_password")
                .phoneNumber("+1987654321")
                .role(Role.LIBRARIAN)
                .address("456 Librarian Ave")
                .createdAt(now)
                .build();

        registrationRequest = UserRegistrationRequest.builder()
                .firstName("New")
                .lastName("User")
                .email("newuser@example.com")
                .password("password123")
                .phoneNumber("+1555555555")
                .role(Role.PATRON)
                .address("789 New St")
                .build();

        updateRequest = UserUpdateRequest.builder()
                .firstName("Updated")
                .lastName("User")
                .email("updated@example.com")
                .password("newpassword")
                .phoneNumber("+1666666666")
                .role(Role.LIBRARIAN)
                .address("321 Updated Rd")
                .build();
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3L);
            return user;
        });

        // Act
        UserResponse result = userService.registerUser(registrationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals(registrationRequest.getFirstName(), result.getFirstName());
        assertEquals(registrationRequest.getLastName(), result.getLastName());
        assertEquals(registrationRequest.getEmail(), result.getEmail());
        assertEquals(registrationRequest.getRole(), result.getRole());
        verify(userRepository).existsByEmail(registrationRequest.getEmail());
        verify(passwordEncoder).encode(registrationRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(registrationRequest);
        });
        verify(userRepository).existsByEmail(registrationRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserAndReturn_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3L);
            return user;
        });

        // Act
        User result = userService.registerUserAndReturn(registrationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals(registrationRequest.getFirstName(), result.getFirstName());
        assertEquals(registrationRequest.getLastName(), result.getLastName());
        assertEquals(registrationRequest.getEmail(), result.getEmail());
        assertEquals(registrationRequest.getRole(), result.getRole());
        verify(userRepository).existsByEmail(registrationRequest.getEmail());
        verify(passwordEncoder).encode(registrationRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(patron, librarian);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(patron.getId(), result.get(0).getId());
        assertEquals(librarian.getId(), result.get(1).getId());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(patron));

        // Act
        UserResponse result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(patron.getId(), result.getId());
        assertEquals(patron.getFirstName(), result.getFirstName());
        assertEquals(patron.getLastName(), result.getLastName());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_NotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
        verify(userRepository).findById(999L);
    }

    @Test
    void updateUser_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(patron));
        when(passwordEncoder.encode(anyString())).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(patron);

        // Act
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(patron.getId(), result.getId());
        assertEquals(updateRequest.getFirstName(), result.getFirstName());
        assertEquals(updateRequest.getLastName(), result.getLastName());
        assertEquals(updateRequest.getEmail(), result.getEmail());
        assertEquals(updateRequest.getRole(), result.getRole());
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode(updateRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_NotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(999L, updateRequest);
        });
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(patron));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.updateUser(1L, updateRequest);
        });
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail(updateRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_PartialUpdate() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(patron));
        when(userRepository.save(any(User.class))).thenReturn(patron);

        // Create partial update request (only first name)
        UserUpdateRequest partialRequest = new UserUpdateRequest();
        partialRequest.setFirstName("UpdatedFirstName");

        // Act
        UserResponse result = userService.updateUser(1L, partialRequest);

        // Assert
        assertNotNull(result);
        assertEquals(patron.getId(), result.getId());
        assertEquals("UpdatedFirstName", result.getFirstName());
        assertEquals(patron.getLastName(), result.getLastName());
        assertEquals(patron.getEmail(), result.getEmail());
        assertEquals(patron.getRole(), result.getRole());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound() {
        // Arrange
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(999L);
        });
        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }
} 