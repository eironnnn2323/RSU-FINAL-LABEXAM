package com.rsu.registration.service;

import com.rsu.registration.dto.StudentRegistrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Housing Service - Handles dormitory allocation for first-year students
 */
@Service
@Slf4j
public class HousingService {

    /**
     * Process housing allocation for first-year students
     */
    public String allocateHousing(StudentRegistrationDTO registration) {
        log.info("🏠 HOUSING SYSTEM: Processing housing allocation for first-year student: {} ({})", 
                registration.getStudentName(), registration.getStudentId());
        
        // Simulate housing allocation logic
        String dormitoryBuilding = determineDormitory(registration.getProgram());
        String roomType = "Double Occupancy";
        
        String result = String.format(
                "Housing allocated - Building: %s, Room Type: %s", 
                dormitoryBuilding, roomType
        );
        
        log.info("🏠 HOUSING SYSTEM: {} assigned to {}", 
                registration.getStudentName(), dormitoryBuilding);
        
        return result;
    }
    
    private String determineDormitory(String program) {
        // Route students to different dorms based on program
        if (program.toLowerCase().contains("engineering") || 
            program.toLowerCase().contains("computer")) {
            return "Tech Hall";
        } else if (program.toLowerCase().contains("business") || 
                   program.toLowerCase().contains("management")) {
            return "Commerce Building";
        } else if (program.toLowerCase().contains("arts") || 
                   program.toLowerCase().contains("humanities")) {
            return "Liberal Arts Residence";
        } else {
            return "Main Campus Dormitory";
        }
    }
}
