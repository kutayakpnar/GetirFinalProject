package com.librarymanagement.librarymanagement.dto;

import com.librarymanagement.librarymanagement.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object for user registration")
public class UserRegistrationRequest {
    @NotBlank(message = "First name is required")
    @Schema(description = "User's first name", example = "John")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Schema(description = "User's password", example = "SecurePassword123", type = "string", format = "password")
    private String password;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    @Schema(description = "User's phone number", example = "+1234567890")
    private String phoneNumber;
    
    @NotNull(message = "Role is required")
    @Schema(description = "User's role in the system", example = "PATRON", allowableValues = {"PATRON", "LIBRARIAN"})
    private Role role;
    
    @NotBlank(message = "Address is required")
    @Schema(description = "User's address", example = "123 Main St, Anytown, AT 12345")
    private String address;
} 