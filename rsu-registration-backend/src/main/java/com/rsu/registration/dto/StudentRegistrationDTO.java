package com.rsu.registration.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Student Registration
 * Used for receiving registration data from the frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRegistrationDTO {

    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Program is required")
    private String program;

    @NotBlank(message = "Year level is required")
    private String yearLevel;
}
