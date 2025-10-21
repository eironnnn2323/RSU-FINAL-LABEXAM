package com.rsu.registration.controller;

import com.rsu.registration.dto.RegistrationResponseDTO;
import com.rsu.registration.dto.StudentRegistrationDTO;
import com.rsu.registration.dto.AggregatedStudentProfile;
import com.rsu.registration.dto.TranslationChainDTO;
import com.rsu.registration.model.StudentRegistration;
import com.rsu.registration.model.ErrorCategory;
import com.rsu.registration.service.StudentRegistrationService;
import com.rsu.registration.service.StudentProfileAggregatorService;
import com.rsu.registration.service.MessageTranslatorService;
import com.rsu.registration.service.RetryService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rsu.registration.integration.RegistrationIntegrationConfig.*;

/**
 * REST API Controller for Student Registration
 * API Gateway: Receives registration requests and sends them to message queue
 * Implements Content-Based Routing information in responses
 * Implements Message Translator Pattern for format conversions
 * Implements Error Handling with retry logic and dead-letter queue
 * Provides aggregated student profile retrieval
 */
@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = "*", allowCredentials = "false", maxAge = 3600)
public class RegistrationController {

    private final RabbitTemplate rabbitTemplate;
    private final StudentRegistrationService registrationService;
    private final StudentProfileAggregatorService aggregatorService;
    private final MessageTranslatorService translatorService;
    private final RetryService retryService;

    /**
     * Submit a student registration
     * This is the API Gateway endpoint that receives registration data from the frontend
     * and sends it to the message queue for asynchronous processing
     * Returns routing information to show which systems will process this registration
     */
    @PostMapping("/submit")
    public ResponseEntity<RegistrationResponseDTO> submitRegistration(
            @Valid @RequestBody StudentRegistrationDTO registrationDTO) {

        log.info("📨 Received registration request for student: {} - Year: {}", 
                registrationDTO.getStudentId(), registrationDTO.getYearLevel());

        try {
            // Execute translation chain (Message Translator Pattern)
            log.info("🔄 Executing message translation chain...");
            TranslationChainDTO translationChain = translatorService.executeTranslationChain(registrationDTO);
            log.info("✅ Translation chain completed in {}ms", translationChain.getTotalTranslationTimeMs());
            
            // Determine routing information before sending to queue
            boolean isFirstYear = isFirstYearStudent(registrationDTO.getYearLevel());
            List<String> routedTo = new ArrayList<>();
            String routingMessage;
            
            if (isFirstYear) {
                routedTo.add("Housing System");
                routingMessage = "🏠 First-year student → Routed to Housing for dormitory allocation";
            } else {
                routedTo.add("Billing System");
                routingMessage = "💰 Returning student → Routed to Billing for fee calculation";
            }
            routedTo.add("Library System");
            routingMessage += " + 📚 Library account activation";
            
            log.info("🔀 Routing decision: {} will be routed to: {}", 
                    registrationDTO.getStudentName(), routedTo);

            // Send message to RabbitMQ queue (EIP Pattern: Message Channel)
            try {
                rabbitTemplate.convertAndSend(
                        REGISTRATION_EXCHANGE,
                        "student.registration.submit",
                        registrationDTO
                );

                log.info("✅ Registration message sent to queue for student: {}", 
                        registrationDTO.getStudentId());
            } catch (Exception rabbitException) {
                log.error("❌ RabbitMQ connection failed: {}", rabbitException.getMessage());
                throw rabbitException; // Re-throw to be caught by outer try-catch
            }

            RegistrationResponseDTO response = RegistrationResponseDTO.builder()
                    .success(true)
                    .message("Registration submitted successfully and routed to appropriate systems")
                    .status("SUBMITTED")
                    .isFirstYear(isFirstYear)
                    .routedTo(routedTo)
                    .routingMessage(routingMessage)
                    .translationChain(translationChain)
                    .build();

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("❌ Error submitting registration: {}", e.getMessage(), e);

            // Capture failed message to error channel for retry
            ErrorCategory errorCategory = determineErrorCategory(e);
            try {
                retryService.captureFailedMessage(
                        registrationDTO,
                        "REGISTRATION_SUBMISSION",
                        errorCategory,
                        e
                );
                log.info("⚠️ Failed message captured to error channel - Will retry automatically");
            } catch (Exception captureError) {
                log.error("❌ Failed to capture error message: {}", captureError.getMessage());
            }

            RegistrationResponseDTO response = RegistrationResponseDTO.builder()
                    .success(false)
                    .message("Registration temporarily failed. We're retrying automatically. Please check back in a moment.")
                    .status("RETRYING")
                    .build();

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }
    }
    
    /**
     * Helper method to determine if student is first year
     */
    private boolean isFirstYearStudent(String yearLevel) {
        if (yearLevel == null) {
            return false;
        }
        String normalizedYearLevel = yearLevel.toLowerCase().trim();
        return normalizedYearLevel.equals("1") || 
               normalizedYearLevel.equals("1st year") ||
               normalizedYearLevel.equals("first year") ||
               normalizedYearLevel.equals("freshman");
    }
    
    /**
     * Determines error category based on exception type
     */
    private ErrorCategory determineErrorCategory(Exception e) {
        String errorMessage = e.getMessage();
        String exceptionType = e.getClass().getSimpleName();
        
        if (errorMessage != null) {
            if (errorMessage.contains("timeout") || errorMessage.contains("Timeout")) {
                return ErrorCategory.NETWORK_TIMEOUT;
            }
            if (errorMessage.contains("connection") || errorMessage.contains("Connection")) {
                return ErrorCategory.SYSTEM_DOWN;
            }
            if (errorMessage.contains("validation") || errorMessage.contains("invalid")) {
                return ErrorCategory.INVALID_DATA;
            }
            if (errorMessage.contains("database") || errorMessage.contains("SQL")) {
                return ErrorCategory.DATABASE_ERROR;
            }
            if (errorMessage.contains("queue") || errorMessage.contains("RabbitMQ")) {
                return ErrorCategory.QUEUE_ERROR;
            }
        }
        
        // Check exception type
        if (exceptionType.contains("Timeout")) {
            return ErrorCategory.NETWORK_TIMEOUT;
        }
        if (exceptionType.contains("Connection")) {
            return ErrorCategory.SYSTEM_DOWN;
        }
        if (exceptionType.contains("Validation")) {
            return ErrorCategory.INVALID_DATA;
        }
        if (exceptionType.contains("SQL") || exceptionType.contains("Database")) {
            return ErrorCategory.DATABASE_ERROR;
        }
        
        return ErrorCategory.UNKNOWN;
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
     * Get registration status including retry information
     * Returns combined status from registration and failed message (if any)
     */
    @GetMapping("/status/{studentId}")
    public ResponseEntity<Map<String, Object>> getRegistrationStatus(@PathVariable String studentId) {
        log.info("Fetching status for student ID: {}", studentId);
        
        Map<String, Object> status = new HashMap<>();
        
        // Check main registration
        StudentRegistration registration = registrationService.getRegistrationByStudentId(studentId);
        if (registration != null) {
            status.put("registered", true);
            status.put("registrationStatus", registration.getStatus());
            status.put("registrationTimestamp", registration.getRegistrationTimestamp());
            status.put("studentName", registration.getStudentName());
            status.put("program", registration.getProgram());
            status.put("yearLevel", registration.getYearLevel());
        } else {
            status.put("registered", false);
        }
        
        // Check for failed messages / retry status
        try {
            var failedMessage = retryService.getMessagesByStatus(null).stream()
                    .filter(msg -> msg.getStudentId().equals(studentId))
                    .findFirst();
            
            if (failedMessage.isPresent()) {
                var msg = failedMessage.get();
                status.put("hasError", true);
                status.put("retryStatus", msg.getStatus());
                status.put("retryAttempts", msg.getRetryAttempts());
                status.put("maxRetryAttempts", msg.getMaxRetryAttempts());
                status.put("nextRetryAt", msg.getNextRetryAt());
                status.put("failedAt", msg.getFailedAt());
                status.put("errorMessage", msg.getErrorMessage());
                status.put("errorCategory", msg.getErrorCategory());
                status.put("inDeadLetterQueue", msg.isInDeadLetterQueue());
                
                // User-friendly message
                String userMessage = generateUserMessage(msg);
                status.put("userMessage", userMessage);
            } else {
                status.put("hasError", false);
            }
        } catch (Exception e) {
            log.error("Error checking failed messages: {}", e.getMessage());
            status.put("hasError", false);
        }
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Generates user-friendly status message
     */
    private String generateUserMessage(com.rsu.registration.model.FailedMessage msg) {
        switch (msg.getStatus()) {
            case PENDING_RETRY:
                return String.format("Registration temporarily failed. We're retrying automatically (Attempt %d/%d). Next retry at: %s", 
                                   msg.getRetryAttempts() + 1, 
                                   msg.getMaxRetryAttempts(),
                                   msg.getNextRetryAt().toString());
            case RETRYING:
                return "We're currently retrying your registration. Please check back in a moment.";
            case RETRY_SUCCESS:
                return "Your registration was successfully processed after automatic retry!";
            case MOVED_TO_DLQ:
            case AWAITING_MANUAL_RETRY:
                return "Registration failed after multiple attempts. Our team has been notified and will process your registration manually. Please check back later or contact support.";
            case MANUAL_RETRY_SUCCESS:
                return "Your registration was successfully processed by our team!";
            case FAILED:
                return "Registration failed. Please contact support with your student ID for assistance.";
            default:
                return "Registration status unknown. Please contact support.";
        }
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
     * Get aggregated student profile
     * Returns complete profile with responses from all systems
     */
    @GetMapping("/profile/{studentId}")
    public ResponseEntity<AggregatedStudentProfile> getStudentProfile(@PathVariable String studentId) {
        log.info("Fetching aggregated profile for student ID: {}", studentId);

        try {
            // First, get the registration from database
            StudentRegistration registration = registrationService.getRegistrationByStudentId(studentId);

            if (registration == null) {
                log.warn("Registration not found for student ID: {}", studentId);
                return ResponseEntity.notFound().build();
            }

            // Convert to DTO
            StudentRegistrationDTO dto = StudentRegistrationDTO.builder()
                    .studentId(registration.getStudentId())
                    .studentName(registration.getStudentName())
                    .email(registration.getEmail())
                    .program(registration.getProgram())
                    .yearLevel(registration.getYearLevel())
                    .build();

            // Get fresh aggregated profile
            AggregatedStudentProfile profile = aggregatorService.aggregateStudentProfile(dto);

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            log.error("Error fetching student profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download registration data as XML
     * Endpoint: GET /api/v1/registrations/download-xml/{studentId}
     */
    @GetMapping(value = "/download-xml/{studentId}", produces = "application/xml")
    public ResponseEntity<String> downloadRegistrationAsXml(@PathVariable String studentId) {
        log.info("📥 Request to download XML for student: {}", studentId);
        
        try {
            StudentRegistration registration = registrationService.getRegistrationByStudentId(studentId);
            
            if (registration == null) {
                log.warn("⚠️ Student not found: {}", studentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("<error><message>Student not found</message></error>");
            }
            
            // Convert to XML format
            String xml = convertToXml(registration);
            
            log.info("✅ XML generated for student: {}", studentId);
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"registration_" + studentId + ".xml\"")
                .body(xml);
                
        } catch (Exception e) {
            log.error("❌ Error generating XML for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("<error><message>" + e.getMessage() + "</message></error>");
        }
    }
    
    /**
     * Helper method to convert StudentRegistration to XML
     */
    private String convertToXml(StudentRegistration registration) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<studentRegistration>\n");
        xml.append("  <id>").append(registration.getId()).append("</id>\n");
        xml.append("  <studentId>").append(escapeXml(registration.getStudentId())).append("</studentId>\n");
        xml.append("  <studentName>").append(escapeXml(registration.getStudentName())).append("</studentName>\n");
        xml.append("  <email>").append(escapeXml(registration.getEmail())).append("</email>\n");
        xml.append("  <program>").append(escapeXml(registration.getProgram())).append("</program>\n");
        xml.append("  <yearLevel>").append(escapeXml(registration.getYearLevel())).append("</yearLevel>\n");
        xml.append("  <status>").append(escapeXml(registration.getStatus())).append("</status>\n");
        xml.append("  <registrationTimestamp>").append(registration.getRegistrationTimestamp()).append("</registrationTimestamp>\n");
        if (registration.getMessage() != null) {
            xml.append("  <message>").append(escapeXml(registration.getMessage())).append("</message>\n");
        }
        xml.append("</studentRegistration>");
        return xml.toString();
    }
    
    /**
     * Helper method to escape XML special characters
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Registration service is running");
    }
}
