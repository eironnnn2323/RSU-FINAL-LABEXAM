package com.rsu.registration.service;

import com.rsu.registration.dto.AcademicRecordsResponse;
import com.rsu.registration.dto.StudentRegistrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Academic Records System Service
 * Simulates responses from the academic records system
 * 
 * In a real system, this would call an external API or service
 */
@Service
@Slf4j
public class AcademicRecordsSystemService {
    
    private final Random random = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Process student enrollment and return academic records
     * Simulates async processing with variable response time
     */
    public CompletableFuture<AcademicRecordsResponse> processEnrollment(StudentRegistrationDTO registration) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate processing time (500ms - 2000ms)
                int processingTime = 500 + random.nextInt(1500);
                Thread.sleep(processingTime);
                
                log.info("📚 ACADEMIC RECORDS SYSTEM: Processing enrollment for {} (took {}ms)", 
                        registration.getStudentName(), processingTime);
                
                // Generate academic records response
                AcademicRecordsResponse response = AcademicRecordsResponse.builder()
                        .studentId(registration.getStudentId())
                        .studentName(registration.getStudentName())
                        .program(registration.getProgram())
                        .enrollmentStatus("ENROLLED")
                        .academicLevel(registration.getYearLevel())
                        .gpa(generateGPA())
                        .enrollmentDate(LocalDateTime.now())
                        .advisorName(assignAdvisor(registration.getProgram()))
                        .responseTimestamp(LocalDateTime.now().format(formatter))
                        .build();
                
                log.info("📚 ACADEMIC RECORDS: {} enrolled in {} - Status: ENROLLED", 
                        registration.getStudentName(), registration.getProgram());
                
                return response;
                
            } catch (InterruptedException e) {
                log.error("❌ Academic Records processing interrupted", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Academic Records processing failed", e);
            }
        });
    }
    
    /**
     * Generate a random GPA between 2.0 and 4.0
     */
    private Double generateGPA() {
        return 2.0 + (random.nextDouble() * 2.0);
    }
    
    /**
     * Assign advisor based on program
     */
    private String assignAdvisor(String program) {
        switch (program.toLowerCase()) {
            case "computer science":
                return "Dr. Alan Turing";
            case "business administration":
                return "Prof. Peter Drucker";
            case "engineering":
                return "Dr. Nikola Tesla";
            case "nursing":
                return "Dr. Florence Nightingale";
            case "education":
                return "Prof. John Dewey";
            case "liberal arts":
                return "Dr. Martha Nussbaum";
            default:
                return "Dr. General Advisor";
        }
    }
}
