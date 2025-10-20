package com.rsu.registration.service;

import com.rsu.registration.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Student Profile Aggregator Service
 * 
 * EIP Pattern: Aggregator
 * Combines responses from multiple systems into a single comprehensive student profile
 * 
 * Systems aggregated:
 * 1. Academic Records System (enrollment confirmation)
 * 2. Housing/Billing System (based on year level)
 * 3. Library Services System (for all students)
 * 
 * Implements timeout handling for slow systems (30 seconds default)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProfileAggregatorService {
    
    private final AcademicRecordsSystemService academicRecordsSystem;
    private final HousingSystemService housingSystem;
    private final BillingSystemService billingSystem;
    private final LibrarySystemService librarySystem;
    
    private static final long AGGREGATION_TIMEOUT_SECONDS = 30;
    
    /**
     * Aggregate student profile from all systems
     * 
     * @param registration Student registration data
     * @return Aggregated student profile with all system responses
     */
    public AggregatedStudentProfile aggregateStudentProfile(StudentRegistrationDTO registration) {
        log.info("🔄 AGGREGATOR: Starting aggregation for student: {}", registration.getStudentName());
        long startTime = System.currentTimeMillis();
        
        try {
            // Determine if student is first year
            boolean isFirstYear = isFirstYearStudent(registration.getYearLevel());
            
            log.info("🔄 AGGREGATOR: Initiating {} system calls for {}", 
                    isFirstYear ? "Academic, Housing, Library" : "Academic, Billing, Library",
                    registration.getStudentName());
            
            // Start all system calls asynchronously
            CompletableFuture<AcademicRecordsResponse> academicFuture = 
                    academicRecordsSystem.processEnrollment(registration);
            
            CompletableFuture<HousingResponse> housingFuture = isFirstYear ? 
                    housingSystem.processHousingAssignment(registration) : null;
            
            CompletableFuture<BillingResponse> billingFuture = !isFirstYear ? 
                    billingSystem.processBillingSetup(registration) : null;
            
            CompletableFuture<LibraryResponse> libraryFuture = 
                    librarySystem.processLibraryAccountActivation(registration);
            
            // Wait for all responses with timeout
            CompletableFuture<Void> allFutures;
            if (isFirstYear) {
                allFutures = CompletableFuture.allOf(academicFuture, housingFuture, libraryFuture);
            } else {
                allFutures = CompletableFuture.allOf(academicFuture, billingFuture, libraryFuture);
            }
            
            // Apply timeout
            allFutures.get(AGGREGATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            // Collect all responses
            AcademicRecordsResponse academic = academicFuture.getNow(null);
            HousingResponse housing = housingFuture != null ? housingFuture.getNow(null) : null;
            BillingResponse billing = billingFuture != null ? billingFuture.getNow(null) : null;
            LibraryResponse library = libraryFuture.getNow(null);
            
            long endTime = System.currentTimeMillis();
            long aggregationTime = endTime - startTime;
            
            // Count responses
            int responsesReceived = countResponses(academic, housing, billing, library);
            boolean isComplete = (academic != null && (housing != null || billing != null) && library != null);
            
            // Build aggregated profile
            AggregatedStudentProfile profile = AggregatedStudentProfile.builder()
                    .studentId(registration.getStudentId())
                    .studentName(registration.getStudentName())
                    .program(registration.getProgram())
                    .yearLevel(registration.getYearLevel())
                    .academicRecords(academic)
                    .housing(housing)
                    .billing(billing)
                    .library(library)
                    .aggregationTimestamp(LocalDateTime.now())
                    .responsesReceived(responsesReceived)
                    .responsesExpected(3)
                    .isComplete(isComplete)
                    .aggregationTimeMs(aggregationTime)
                    .aggregationStatus(determineAggregationStatus(academic, housing, billing, library))
                    .build();
            
            log.info("✅ AGGREGATOR: Successfully aggregated profile for {} in {}ms", 
                    registration.getStudentName(), aggregationTime);
            log.info("📊 AGGREGATOR: Responses - Academic: {}, Housing/Billing: {}, Library: {}", 
                    academic != null ? "✓" : "✗",
                    (housing != null || billing != null) ? "✓" : "✗",
                    library != null ? "✓" : "✗");
            
            return profile;
            
        } catch (TimeoutException e) {
            log.error("⏱️ AGGREGATOR: Timeout after {}s for student: {}", 
                    AGGREGATION_TIMEOUT_SECONDS, registration.getStudentName());
            return createPartialProfile(registration, startTime, "TIMEOUT");
            
        } catch (Exception e) {
            log.error("❌ AGGREGATOR: Error aggregating profile for {}: {}", 
                    registration.getStudentName(), e.getMessage(), e);
            return createPartialProfile(registration, startTime, "ERROR");
        }
    }
    
    /**
     * Determine if student is first year
     */
    private boolean isFirstYearStudent(String yearLevel) {
        if (yearLevel == null) return false;
        String normalized = yearLevel.toLowerCase().trim();
        return normalized.equals("1") || 
               normalized.equals("first year") ||
               normalized.contains("first");
    }
    
    /**
     * Count how many responses were successfully received
     */
    private int countResponses(AcademicRecordsResponse academic, 
                               HousingResponse housing, 
                               BillingResponse billing, 
                               LibraryResponse library) {
        int count = 0;
        if (academic != null) count++;
        if (housing != null || billing != null) count++;
        if (library != null) count++;
        return count;
    }
    
    /**
     * Determine aggregation status based on received responses
     */
    private String determineAggregationStatus(AcademicRecordsResponse academic, 
                                              HousingResponse housing, 
                                              BillingResponse billing, 
                                              LibraryResponse library) {
        int received = countResponses(academic, housing, billing, library);
        if (received == 3) return "COMPLETE";
        if (received > 0) return "PARTIAL";
        return "FAILED";
    }
    
    /**
     * Create a partial profile when some systems fail or timeout
     */
    private AggregatedStudentProfile createPartialProfile(StudentRegistrationDTO registration, 
                                                          long startTime, 
                                                          String status) {
        long endTime = System.currentTimeMillis();
        
        return AggregatedStudentProfile.builder()
                .studentId(registration.getStudentId())
                .studentName(registration.getStudentName())
                .program(registration.getProgram())
                .yearLevel(registration.getYearLevel())
                .aggregationTimestamp(LocalDateTime.now())
                .responsesReceived(0)
                .responsesExpected(3)
                .isComplete(false)
                .aggregationTimeMs(endTime - startTime)
                .aggregationStatus(status)
                .build();
    }
}
