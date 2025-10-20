package com.rsu.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response from Academic Records System
 * Contains enrollment and academic information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicRecordsResponse {
    private String studentId;
    private String studentName;
    private String program;
    private String enrollmentStatus; // ENROLLED, PENDING, WAITLISTED
    private String academicLevel;
    private Double gpa;
    private LocalDateTime enrollmentDate;
    private String advisorName;
    private String responseTimestamp;
}
