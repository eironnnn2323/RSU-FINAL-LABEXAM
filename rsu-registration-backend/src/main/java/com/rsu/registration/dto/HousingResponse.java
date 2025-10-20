package com.rsu.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response from Housing System (for first-year students)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HousingResponse {
    private String studentId;
    private String roomAssignment;
    private String dormitoryBuilding;
    private String roomType;
    private LocalDate moveInDate;
    private String floorNumber;
    private String roommateName;
    private String housingStatus; // ASSIGNED, PENDING, WAITLIST
    private String responseTimestamp;
}
