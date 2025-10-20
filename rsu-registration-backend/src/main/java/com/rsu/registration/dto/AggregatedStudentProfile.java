package com.rsu.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Aggregated Student Profile
 * Combines responses from Academic Records, Housing/Billing, and Library systems
 * 
 * EIP Pattern: Aggregator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedStudentProfile {
    
    // Student basic info
    private String studentId;
    private String studentName;
    private String program;
    private String yearLevel;
    
    // Academic Records data
    private AcademicRecordsResponse academicRecords;
    
    // Housing OR Billing data (based on year level)
    private HousingResponse housing;
    private BillingResponse billing;
    
    // Library data (always present)
    private LibraryResponse library;
    
    // Aggregation metadata
    private LocalDateTime aggregationTimestamp;
    private Integer responsesReceived;
    private Integer responsesExpected;
    private Boolean isComplete;
    private Long aggregationTimeMs;
    private String aggregationStatus; // COMPLETE, PARTIAL, TIMEOUT
    
    /**
     * Check if all expected responses are received
     */
    public boolean isAggregationComplete() {
        return academicRecords != null && 
               (housing != null || billing != null) && 
               library != null;
    }
    
    /**
     * Get completion percentage
     */
    public double getCompletionPercentage() {
        int received = 0;
        if (academicRecords != null) received++;
        if (housing != null || billing != null) received++;
        if (library != null) received++;
        return (received / 3.0) * 100.0;
    }
}
