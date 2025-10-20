package com.rsu.registration.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Student Registration Entity
 * Represents a student's registration record with timestamp tracking
 */
@Entity
@Table(name = "student_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false, unique = true)
    private String studentId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String program;

    @Column(nullable = false)
    private String yearLevel;

    @Column(nullable = false)
    private LocalDateTime registrationTimestamp;

    @Column(nullable = false)
    private String status; // PENDING, REGISTERED, FAILED

    @Column(length = 500)
    private String message;

    @PrePersist
    protected void onCreate() {
        if (registrationTimestamp == null) {
            registrationTimestamp = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}
