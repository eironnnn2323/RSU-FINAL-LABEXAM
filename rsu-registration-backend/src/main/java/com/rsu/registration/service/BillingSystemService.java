package com.rsu.registration.service;

import com.rsu.registration.dto.BillingResponse;
import com.rsu.registration.dto.StudentRegistrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Billing System Service
 * Simulates responses from the billing/payment system (for returning students)
 */
@Service
@Slf4j
public class BillingSystemService {
    
    private final Random random = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Process billing and fee calculation for returning students
     * Simulates async processing with variable response time
     */
    public CompletableFuture<BillingResponse> processBillingSetup(StudentRegistrationDTO registration) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate processing time (600ms - 2200ms)
                int processingTime = 600 + random.nextInt(1600);
                Thread.sleep(processingTime);
                
                log.info("💰 BILLING SYSTEM: Processing billing for {} (took {}ms)", 
                        registration.getStudentName(), processingTime);
                
                BigDecimal tuition = calculateTuition(registration.getYearLevel());
                BigDecimal miscFees = BigDecimal.valueOf(5000.00);
                BigDecimal total = tuition.add(miscFees);
                
                BillingResponse response = BillingResponse.builder()
                        .studentId(registration.getStudentId())
                        .tuitionFee(tuition)
                        .miscellaneousFees(miscFees)
                        .totalFeeAmount(total)
                        .paymentDeadline(LocalDate.now().plusDays(30))
                        .accountStatus("ACTIVE")
                        .amountPaid(BigDecimal.ZERO)
                        .balanceRemaining(total)
                        .paymentPlan("Full Payment or Installment Available")
                        .responseTimestamp(java.time.LocalDateTime.now().format(formatter))
                        .build();
                
                log.info("💰 BILLING: {} - Total: ₱{}, Deadline: {}", 
                        registration.getStudentName(), total, response.getPaymentDeadline());
                
                return response;
                
            } catch (InterruptedException e) {
                log.error("❌ Billing processing interrupted", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Billing processing failed", e);
            }
        });
    }
    
    private BigDecimal calculateTuition(String yearLevel) {
        BigDecimal baseFee = BigDecimal.valueOf(30000.00);
        
        switch (yearLevel.toLowerCase()) {
            case "2":
            case "second year":
                return baseFee.multiply(BigDecimal.valueOf(1.05));
            case "3":
            case "third year":
                return baseFee.multiply(BigDecimal.valueOf(1.10));
            case "4":
            case "fourth year":
                return baseFee.multiply(BigDecimal.valueOf(1.15));
            default:
                return baseFee;
        }
    }
}
