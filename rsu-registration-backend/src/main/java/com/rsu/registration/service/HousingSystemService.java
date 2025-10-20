package com.rsu.registration.service;

import com.rsu.registration.dto.HousingResponse;
import com.rsu.registration.dto.StudentRegistrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Housing System Service
 * Simulates responses from the housing/dormitory system (for first-year students)
 */
@Service
@Slf4j
public class HousingSystemService {
    
    private final Random random = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Process housing assignment for first-year students
     * Simulates async processing with variable response time
     */
    public CompletableFuture<HousingResponse> processHousingAssignment(StudentRegistrationDTO registration) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate processing time (800ms - 2500ms)
                int processingTime = 800 + random.nextInt(1700);
                Thread.sleep(processingTime);
                
                log.info("🏠 HOUSING SYSTEM: Processing housing assignment for {} (took {}ms)", 
                        registration.getStudentName(), processingTime);
                
                String building = determineDormitory(registration.getProgram());
                String roomNumber = generateRoomNumber();
                
                HousingResponse response = HousingResponse.builder()
                        .studentId(registration.getStudentId())
                        .roomAssignment(roomNumber)
                        .dormitoryBuilding(building)
                        .roomType("Double Occupancy")
                        .moveInDate(LocalDate.now().plusDays(14)) // 2 weeks from now
                        .floorNumber(roomNumber.substring(0, 1))
                        .roommateName(generateRoommateName())
                        .housingStatus("ASSIGNED")
                        .responseTimestamp(java.time.LocalDateTime.now().format(formatter))
                        .build();
                
                log.info("🏠 HOUSING: {} assigned to {} - Room {}", 
                        registration.getStudentName(), building, roomNumber);
                
                return response;
                
            } catch (InterruptedException e) {
                log.error("❌ Housing processing interrupted", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Housing processing failed", e);
            }
        });
    }
    
    private String determineDormitory(String program) {
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
    
    private String generateRoomNumber() {
        int floor = 1 + random.nextInt(5); // Floors 1-5
        int room = 1 + random.nextInt(20); // Rooms 1-20
        return String.format("%d%02d", floor, room);
    }
    
    private String generateRoommateName() {
        String[] firstNames = {"Alex", "Jordan", "Taylor", "Morgan", "Casey", "Riley"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Davis", "Miller"};
        return firstNames[random.nextInt(firstNames.length)] + " " + 
               lastNames[random.nextInt(lastNames.length)];
    }
}
