package com.rsu.registration.service;

import com.rsu.registration.dto.StudentRegistrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Library Service - Handles library account activation for all students
 */
@Service
@Slf4j
public class LibraryService {

    /**
     * Process library account activation for all students
     */
    public String activateLibraryAccount(StudentRegistrationDTO registration) {
        log.info("📚 LIBRARY SYSTEM: Activating library account for: {} ({})", 
                registration.getStudentName(), registration.getStudentId());
        
        // Generate library card number
        String libraryCardNumber = generateLibraryCardNumber(registration.getStudentId());
        
        // Set account expiration date (1 year from now)
        LocalDate expirationDate = LocalDate.now().plusYears(1);
        
        String result = String.format(
                "Library account activated - Card #: %s, Valid until: %s, Loan limit: 5 books", 
                libraryCardNumber, expirationDate
        );
        
        log.info("📚 LIBRARY SYSTEM: {} - Library Card #{} activated", 
                registration.getStudentName(), libraryCardNumber);
        
        return result;
    }
    
    private String generateLibraryCardNumber(String studentId) {
        // Generate library card number from student ID
        return "LIB-" + studentId.replace("-", "");
    }
}
