package com.rsu.registration.controller;

import com.rsu.registration.dto.RegistrationResponseDTO;
import com.rsu.registration.dto.StudentRegistrationDTO;
import com.rsu.registration.model.StudentRegistration;
import com.rsu.registration.service.StudentRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.rsu.registration.integration.RegistrationIntegrationConfig.*;

/**
 * REST API Controller for Student Registration
 * API Gateway: Receives registration requests and sends them to message queue
 */
@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class RegistrationController {

    private final RabbitTemplate rabbitTemplate;
    private final StudentRegistrationService registrationService;

    /**
     * Submit a student registration
     * This is the API Gateway endpoint that receives registration data from the frontend
     * and sends it to the message queue for asynchronous processing
     */
    @PostMapping("/submit")
    public ResponseEntity<RegistrationResponseDTO> submitRegistration(
            @Valid @RequestBody StudentRegistrationDTO registrationDTO) {

        log.info("Received registration request for student: {}", registrationDTO.getStudentId());

        try {
            // Send message to RabbitMQ queue (EIP Pattern: Message Channel)
            rabbitTemplate.convertAndSend(
                    REGISTRATION_EXCHANGE,
                    "student.registration.submit",
                    registrationDTO
            );

            log.info("Registration message sent to queue for student: {}", registrationDTO.getStudentId());

            RegistrationResponseDTO response = RegistrationResponseDTO.builder()
                    .success(true)
                    .message("Registration submitted successfully. Processing...")
                    .status("SUBMITTED")
                    .build();

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Error submitting registration: {}", e.getMessage(), e);

            RegistrationResponseDTO response = RegistrationResponseDTO.builder()
                    .success(false)
                    .message("Failed to submit registration: " + e.getMessage())
                    .status("FAILED")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get registration status by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentRegistration> getRegistration(@PathVariable Long id) {
        log.info("Fetching registration with ID: {}", id);

        StudentRegistration registration = registrationService.getRegistrationById(id);

        if (registration == null) {
            log.warn("Registration not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(registration);
    }

    /**
     * Get registration by student ID
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentRegistration> getRegistrationByStudentId(@PathVariable String studentId) {
        log.info("Fetching registration for student ID: {}", studentId);

        StudentRegistration registration = registrationService.getRegistrationByStudentId(studentId);

        if (registration == null) {
            log.warn("Registration not found for student ID: {}", studentId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(registration);
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getRegistrationCount() {
        log.info("Fetching registration count");
        long count = registrationService.getRegistrationCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Registration service is running");
    }
}
