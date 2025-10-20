package com.rsu.registration.service;

import com.rsu.registration.dto.LibraryResponse;
import com.rsu.registration.dto.StudentRegistrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Library Services System Service
 * Simulates responses from the library system (for all students)
 */
@Service
@Slf4j
public class LibrarySystemService {
    
    private final Random random = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Process library account activation for all students
     * Simulates async processing with variable response time
     */
    public CompletableFuture<LibraryResponse> processLibraryAccountActivation(StudentRegistrationDTO registration) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate processing time (400ms - 1800ms)
                int processingTime = 400 + random.nextInt(1400);
                Thread.sleep(processingTime);
                
                log.info("📚 LIBRARY SYSTEM: Processing library account for {} (took {}ms)", 
                        registration.getStudentName(), processingTime);
                
                String libraryCardNumber = generateLibraryCardNumber(registration.getStudentId());
                List<String> services = getAvailableServices(registration.getYearLevel());
                
                LibraryResponse response = LibraryResponse.builder()
                        .studentId(registration.getStudentId())
                        .libraryCardNumber(libraryCardNumber)
                        .accountStatus("ACTIVE")
                        .expirationDate(LocalDate.now().plusYears(1))
                        .maxBooksAllowed(getMaxBooks(registration.getYearLevel()))
                        .currentBooksCheckedOut(0)
                        .availableServices(services)
                        .outstandingFines(BigDecimal.ZERO)
                        .accessLevel(determineAccessLevel(registration.getYearLevel()))
                        .responseTimestamp(java.time.LocalDateTime.now().format(formatter))
                        .build();
                
                log.info("📚 LIBRARY: {} - Card #{}, Max Books: {}", 
                        registration.getStudentName(), libraryCardNumber, response.getMaxBooksAllowed());
                
                return response;
                
            } catch (InterruptedException e) {
                log.error("❌ Library processing interrupted", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Library processing failed", e);
            }
        });
    }
    
    private String generateLibraryCardNumber(String studentId) {
        return "LIB-" + studentId.replace("-", "");
    }
    
    private Integer getMaxBooks(String yearLevel) {
        if (yearLevel.toLowerCase().contains("first") || yearLevel.equals("1")) {
            return 5;
        } else if (yearLevel.toLowerCase().contains("second") || yearLevel.equals("2")) {
            return 7;
        } else if (yearLevel.toLowerCase().contains("third") || yearLevel.equals("3")) {
            return 10;
        } else {
            return 12; // Fourth year and above
        }
    }
    
    private List<String> getAvailableServices(String yearLevel) {
        List<String> baseServices = Arrays.asList(
                "Book Borrowing",
                "Digital Resources",
                "Study Rooms",
                "Computer Lab Access",
                "Printing Services"
        );
        
        // Upper years get additional services
        if (!yearLevel.toLowerCase().contains("first") && !yearLevel.equals("1")) {
            return Arrays.asList(
                    "Book Borrowing",
                    "Digital Resources",
                    "Study Rooms",
                    "Computer Lab Access",
                    "Printing Services",
                    "Research Database Access",
                    "Interlibrary Loan"
            );
        }
        
        return baseServices;
    }
    
    private String determineAccessLevel(String yearLevel) {
        return "UNDERGRADUATE";
    }
}
