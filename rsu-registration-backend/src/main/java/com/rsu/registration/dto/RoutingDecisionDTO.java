package com.rsu.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for representing routing decisions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingDecisionDTO {
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
