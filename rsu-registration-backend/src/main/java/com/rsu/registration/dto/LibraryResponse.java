package com.rsu.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response from Library Services System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryResponse {
    private String studentId;
    private String libraryCardNumber;
    private String accountStatus; // ACTIVE, INACTIVE, SUSPENDED
    private LocalDate expirationDate;
    private Integer maxBooksAllowed;
    private Integer currentBooksCheckedOut;
    private List<String> availableServices;
    private BigDecimal outstandingFines;
    private String accessLevel; // UNDERGRADUATE, GRADUATE, FACULTY
    private String responseTimestamp;
}
