package com.rsu.registration.service;

import com.rsu.registration.dto.StudentRegistrationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Content-Based Router Service
 * Routes student registrations to appropriate systems based on year level
 * 
 * EIP Pattern: Content-Based Router
 * - Examines message content (year level)
 * - Routes to different channels based on content
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentBasedRouterService {

    private final HousingService housingService;
    private final BillingService billingService;
    private final LibraryService libraryService;

    /**
     * Route student registration to appropriate systems based on year level
     * 
     * Routing Logic:
     * - First year students (1, 1st Year) → Housing System
     * - Returning students (2-4, 2nd-4th Year) → Billing System
     * - All students → Library System
     */
    public RoutingResult routeRegistration(StudentRegistrationDTO registration) {
        log.info("🔀 CONTENT-BASED ROUTER: Routing registration for {} - Year: {}", 
                registration.getStudentName(), registration.getYearLevel());
        
        RoutingResult result = new RoutingResult();
        result.setStudentId(registration.getStudentId());
        result.setStudentName(registration.getStudentName());
        result.setYearLevel(registration.getYearLevel());
        result.setProgram(registration.getProgram());
        
        List<String> routedTo = new ArrayList<>();
        
        // Determine if student is first year
        boolean isFirstYear = isFirstYearStudent(registration.getYearLevel());
        result.setFirstYear(isFirstYear);
        
        // Route based on year level
        if (isFirstYear) {
            // First-year students → Housing System
            log.info("🔀 ROUTER: Routing {} to HOUSING SYSTEM (First Year Student)", 
                    registration.getStudentName());
            String housingResult = housingService.allocateHousing(registration);
            result.setHousingStatus(housingResult);
            routedTo.add("Housing System");
        } else {
            // Returning students → Billing System
            log.info("🔀 ROUTER: Routing {} to BILLING SYSTEM (Returning Student)", 
                    registration.getStudentName());
            String billingResult = billingService.processBilling(registration);
            result.setBillingStatus(billingResult);
            routedTo.add("Billing System");
        }
        
        // All students → Library System
        log.info("🔀 ROUTER: Routing {} to LIBRARY SYSTEM (All Students)", 
                registration.getStudentName());
        String libraryResult = libraryService.activateLibraryAccount(registration);
        result.setLibraryStatus(libraryResult);
        routedTo.add("Library System");
        
        result.setRoutedTo(routedTo);
        
        log.info("✅ ROUTER: Completed routing for {} to systems: {}", 
                registration.getStudentName(), routedTo);
        
        return result;
    }
    
    /**
     * Determine if student is a first-year student
     */
    private boolean isFirstYearStudent(String yearLevel) {
        if (yearLevel == null) {
            return false;
        }
        
        String normalizedYearLevel = yearLevel.toLowerCase().trim();
        
        return normalizedYearLevel.equals("1") || 
               normalizedYearLevel.equals("1st year") ||
               normalizedYearLevel.equals("first year") ||
               normalizedYearLevel.equals("freshman");
    }
    
    /**
     * Inner class to hold routing results
     */
    @lombok.Data
    public static class RoutingResult {
        private String studentId;
        private String studentName;
        private String yearLevel;
        private String program;
        private boolean isFirstYear;
        private List<String> routedTo;
        private String housingStatus;
        private String billingStatus;
        private String libraryStatus;
    }
}
