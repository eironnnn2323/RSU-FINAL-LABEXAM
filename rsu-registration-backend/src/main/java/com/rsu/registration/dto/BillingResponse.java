package com.rsu.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response from Billing System (for returning students)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingResponse {
    private String studentId;
    private BigDecimal totalFeeAmount;
    private BigDecimal tuitionFee;
    private BigDecimal miscellaneousFees;
    private LocalDate paymentDeadline;
    private String accountStatus; // ACTIVE, PENDING_PAYMENT, OVERDUE
    private BigDecimal amountPaid;
    private BigDecimal balanceRemaining;
    private String paymentPlan;
    private String responseTimestamp;
}
