package com.librarymanagement.librarymanagement.dto;

import com.librarymanagement.librarymanagement.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String password;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;
    
    private Role role;
    private String address;
} 