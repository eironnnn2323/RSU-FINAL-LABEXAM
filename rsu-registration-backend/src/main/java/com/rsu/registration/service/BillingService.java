package com.rsu.registration.service;

import com.rsu.registration.dto.StudentRegistrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Billing Service - Handles fee calculation and payment setup for returning students
 */
@Service
@Slf4j
public class BillingService {

    /**
     * Process billing and fee calculation for returning students
     */
    public String processBilling(StudentRegistrationDTO registration) {
        log.info("💰 BILLING SYSTEM: Processing fee calculation for returning student: {} ({})", 
                registration.getStudentName(), registration.getStudentId());
        
        // Calculate fees based on year level and program
        BigDecimal tuitionFee = calculateTuition(registration.getYearLevel(), registration.getProgram());
        BigDecimal miscFees = BigDecimal.valueOf(5000.00);
        BigDecimal totalFee = tuitionFee.add(miscFees);
        
        String result = String.format(
                "Billing processed - Tuition: ₱%.2f, Misc Fees: ₱%.2f, Total: ₱%.2f", 
                tuitionFee, miscFees, totalFee
        );
        
        log.info("💰 BILLING SYSTEM: {} - Total fees: ₱{}", 
                registration.getStudentName(), totalFee);
        
        return result;
    }
    
    private BigDecimal calculateTuition(String yearLevel, String program) {
        BigDecimal baseFee = BigDecimal.valueOf(30000.00);
        
        // Adjust based on year level (higher years may have specialized courses)
        switch (yearLevel) {
            case "2":
            case "2nd Year":
                return baseFee.multiply(BigDecimal.valueOf(1.05));
            case "3":
            case "3rd Year":
                return baseFee.multiply(BigDecimal.valueOf(1.10));
            case "4":
            case "4th Year":
                return baseFee.multiply(BigDecimal.valueOf(1.15));
            default:
                return baseFee;
        }
    }
}
