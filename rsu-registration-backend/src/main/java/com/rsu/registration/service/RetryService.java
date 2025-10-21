package com.rsu.registration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsu.registration.dto.StudentRegistrationDTO;
import com.rsu.registration.model.ErrorCategory;
import com.rsu.registration.model.FailedMessage;
import com.rsu.registration.model.RetryStatus;
import com.rsu.registration.repository.FailedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Service for handling failed messages with automatic retry logic and exponential backoff.
 * 
 * Retry Schedule:
 * - Attempt 1: After 5 seconds
 * - Attempt 2: After 10 seconds (from previous attempt)
 * - Attempt 3: After 20 seconds (from previous attempt)
 * 
 * After 3 failed attempts, message is moved to Dead-Letter Queue (DLQ)
 * for manual intervention.
 * 
 * Features:
 * - Automatic retry with exponential backoff
 * - Dead-letter queue for failed messages
 * - Manual retry capability
 * - User notification tracking
 * - Comprehensive error logging
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RetryService {
    
    private final FailedMessageRepository failedMessageRepository;
    private final ErrorLogService errorLogService;
    private final ObjectMapper objectMapper;
    
    // Retry delays in seconds: 5, 10, 20
    private static final int[] RETRY_DELAYS = {5, 10, 20};
    
    /**
     * Captures a failed message to the error channel for retry processing.
     * 
     * @param registrationDTO The original registration data
     * @param failureStage Stage where failure occurred
     * @param errorCategory Category of error
     * @param exception The exception that caused the failure
     * @return The saved FailedMessage entity
     */
    @Transactional
    public FailedMessage captureFailedMessage(
            StudentRegistrationDTO registrationDTO,
            String failureStage,
            ErrorCategory errorCategory,
            Exception exception) {
        
        log.error("❌ [ERROR CHANNEL] Capturing failed message for student: {} at stage: {}", 
                  registrationDTO.getStudentId(), failureStage);
        
        try {
            // Convert registration DTO to JSON
            String originalMessage = objectMapper.writeValueAsString(registrationDTO);
            
            // Get stack trace
            String stackTrace = getStackTrace(exception);
            
            // Calculate next retry time (5 seconds from now for first attempt)
            LocalDateTime nextRetryAt = LocalDateTime.now().plusSeconds(RETRY_DELAYS[0]);
            
            // Build failed message
            FailedMessage failedMessage = FailedMessage.builder()
                    .studentId(registrationDTO.getStudentId())
                    .studentName(registrationDTO.getStudentName())
                    .email(registrationDTO.getEmail())
                    .originalMessage(originalMessage)
                    .failureStage(failureStage)
                    .errorCategory(errorCategory)
                    .errorMessage(exception.getMessage())
                    .stackTrace(stackTrace)
                    .status(RetryStatus.PENDING_RETRY)
                    .retryAttempts(0)
                    .maxRetryAttempts(3)
                    .failedAt(LocalDateTime.now())
                    .nextRetryAt(nextRetryAt)
                    .inDeadLetterQueue(false)
                    .userNotified(false)
                    .retryHistory(buildRetryHistoryEntry(0, "Initial failure", exception.getMessage()))
                    .build();
            
            // Save to database
            failedMessage = failedMessageRepository.save(failedMessage);
            
            // Log to error log service
            errorLogService.logError(
                    registrationDTO.getStudentId(),
                    registrationDTO.getStudentName(),
                    failureStage,
                    errorCategory,
                    exception.getMessage(),
                    stackTrace
            );
            
            log.info("💾 [ERROR CHANNEL] Failed message saved with ID: {} - Next retry at: {}", 
                     failedMessage.getId(), nextRetryAt.format(DateTimeFormatter.ISO_LOCAL_TIME));
            
            return failedMessage;
            
        } catch (Exception e) {
            log.error("❌ [ERROR CHANNEL] Failed to capture error message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to capture error message", e);
        }
    }
    
    /**
     * Scheduled task that runs every 5 seconds to process retry queue.
     * Checks for messages ready for retry and processes them.
     */
    // Temporarily disabled to prevent infinite retries
    // @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    // @Transactional
    public void processRetryQueue() {
        LocalDateTime currentTime = LocalDateTime.now();
        
        // Find messages ready for retry
        List<FailedMessage> readyForRetry = failedMessageRepository
                .findMessagesReadyForRetry(RetryStatus.PENDING_RETRY, currentTime);
        
        if (!readyForRetry.isEmpty()) {
            log.info("🔄 [RETRY SCHEDULER] Found {} messages ready for retry", readyForRetry.size());
            
            for (FailedMessage failedMessage : readyForRetry) {
                processRetry(failedMessage);
            }
        }
        
        // Check for messages that should move to DLQ
        processDeadLetterQueue();
    }
    
    /**
     * Processes retry for a single failed message.
     * 
     * @param failedMessage The message to retry
     */
    @Transactional
    public void processRetry(FailedMessage failedMessage) {
        int currentAttempt = failedMessage.getRetryAttempts() + 1;
        
        log.info("🔄 [RETRY] Attempting retry #{} for student: {} (Failed at: {})", 
                 currentAttempt, 
                 failedMessage.getStudentId(),
                 failedMessage.getFailureStage());
        
        try {
            // Update status to RETRYING
            failedMessage.setStatus(RetryStatus.RETRYING);
            failedMessage.setLastRetryAt(LocalDateTime.now());
            failedMessageRepository.save(failedMessage);
            
            // Parse original message
            StudentRegistrationDTO registrationDTO = objectMapper.readValue(
                    failedMessage.getOriginalMessage(), 
                    StudentRegistrationDTO.class
            );
            
            // TODO: Attempt to reprocess the registration
            // This will be implemented when we integrate with the main registration flow
            // For now, we'll simulate success/failure
            boolean retrySuccess = attemptReprocessing(registrationDTO, failedMessage.getFailureStage());
            
            if (retrySuccess) {
                // Retry succeeded!
                handleRetrySuccess(failedMessage);
            } else {
                // Retry failed, schedule next attempt or move to DLQ
                handleRetryFailure(failedMessage, currentAttempt);
            }
            
        } catch (Exception e) {
            log.error("❌ [RETRY] Error during retry processing: {}", e.getMessage(), e);
            handleRetryFailure(failedMessage, currentAttempt);
        }
    }
    
    /**
     * Handles successful retry.
     */
    private void handleRetrySuccess(FailedMessage failedMessage) {
        log.info("✅ [RETRY SUCCESS] Message successfully processed for student: {}", 
                 failedMessage.getStudentId());
        
        failedMessage.setStatus(RetryStatus.RETRY_SUCCESS);
        failedMessage.setResolvedAt(LocalDateTime.now());
        failedMessage.setRetryHistory(failedMessage.getRetryHistory() + "\n" +
                buildRetryHistoryEntry(failedMessage.getRetryAttempts() + 1, 
                                      "SUCCESS", 
                                      "Message successfully processed"));
        
        failedMessageRepository.save(failedMessage);
        
        // Log success to error log
        errorLogService.logResolution(
                failedMessage.getStudentId(),
                failedMessage.getFailureStage(),
                "Automatic retry succeeded after " + (failedMessage.getRetryAttempts() + 1) + " attempts"
        );
    }
    
    /**
     * Handles failed retry attempt.
     */
    private void handleRetryFailure(FailedMessage failedMessage, int currentAttempt) {
        failedMessage.setRetryAttempts(currentAttempt);
        
        if (currentAttempt >= failedMessage.getMaxRetryAttempts()) {
            // Exhausted all retries, prepare for DLQ
            log.warn("⚠️ [RETRY] All retry attempts exhausted for student: {} - Moving to DLQ", 
                     failedMessage.getStudentId());
            
            failedMessage.setStatus(RetryStatus.MOVED_TO_DLQ);
            failedMessage.setRetryHistory(failedMessage.getRetryHistory() + "\n" +
                    buildRetryHistoryEntry(currentAttempt, 
                                          "FAILED", 
                                          "All retry attempts exhausted"));
            
        } else {
            // Schedule next retry with exponential backoff
            int delaySeconds = RETRY_DELAYS[Math.min(currentAttempt, RETRY_DELAYS.length - 1)];
            LocalDateTime nextRetryAt = LocalDateTime.now().plusSeconds(delaySeconds);
            
            log.info("⏰ [RETRY] Scheduling retry #{} for student: {} in {} seconds", 
                     currentAttempt + 1, 
                     failedMessage.getStudentId(), 
                     delaySeconds);
            
            failedMessage.setStatus(RetryStatus.PENDING_RETRY);
            failedMessage.setNextRetryAt(nextRetryAt);
            failedMessage.setRetryHistory(failedMessage.getRetryHistory() + "\n" +
                    buildRetryHistoryEntry(currentAttempt, 
                                          "FAILED", 
                                          "Scheduling next retry in " + delaySeconds + " seconds"));
        }
        
        failedMessageRepository.save(failedMessage);
    }
    
    /**
     * Moves messages to Dead-Letter Queue (DLQ) when retries are exhausted.
     */
    @Transactional
    public void processDeadLetterQueue() {
        List<FailedMessage> messagesForDlq = failedMessageRepository
                .findMessagesForDeadLetterQueue(RetryStatus.PENDING_RETRY);
        
        if (!messagesForDlq.isEmpty()) {
            log.warn("💀 [DEAD-LETTER QUEUE] Moving {} messages to DLQ", messagesForDlq.size());
            
            for (FailedMessage message : messagesForDlq) {
                message.setInDeadLetterQueue(true);
                message.setStatus(RetryStatus.AWAITING_MANUAL_RETRY);
                message.setMovedToDlqAt(LocalDateTime.now());
                message.setRetryHistory(message.getRetryHistory() + "\n" +
                        buildRetryHistoryEntry(message.getRetryAttempts(), 
                                              "MOVED_TO_DLQ", 
                                              "Message moved to Dead-Letter Queue for manual intervention"));
                
                failedMessageRepository.save(message);
                
                log.warn("💀 [DLQ] Student: {} - Stage: {} - Requires manual intervention", 
                         message.getStudentId(), message.getFailureStage());
            }
        }
    }
    
    /**
     * Manually retry a failed message (admin action).
     * 
     * @param messageId ID of the failed message
     * @param adminUser Admin who initiated the retry
     * @param adminNotes Optional notes from admin
     * @return true if retry was successful
     */
    @Transactional
    public boolean manualRetry(Long messageId, String adminUser, String adminNotes) {
        log.info("👤 [MANUAL RETRY] Admin '{}' initiating manual retry for message ID: {}", 
                 adminUser, messageId);
        
        FailedMessage failedMessage = failedMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Failed message not found: " + messageId));
        
        try {
            // Update metadata
            failedMessage.setRetriedBy(adminUser);
            failedMessage.setAdminNotes(adminNotes);
            failedMessage.setStatus(RetryStatus.RETRYING);
            failedMessage.setLastRetryAt(LocalDateTime.now());
            
            // Parse and reprocess
            StudentRegistrationDTO registrationDTO = objectMapper.readValue(
                    failedMessage.getOriginalMessage(), 
                    StudentRegistrationDTO.class
            );
            
            boolean success = attemptReprocessing(registrationDTO, failedMessage.getFailureStage());
            
            if (success) {
                failedMessage.setStatus(RetryStatus.MANUAL_RETRY_SUCCESS);
                failedMessage.setResolvedAt(LocalDateTime.now());
                failedMessage.setRetryHistory(failedMessage.getRetryHistory() + "\n" +
                        buildRetryHistoryEntry(failedMessage.getRetryAttempts() + 1, 
                                              "MANUAL_SUCCESS", 
                                              "Manual retry by " + adminUser + " - " + adminNotes));
                
                log.info("✅ [MANUAL RETRY SUCCESS] Message ID: {} processed successfully", messageId);
            } else {
                failedMessage.setStatus(RetryStatus.FAILED);
                failedMessage.setRetryHistory(failedMessage.getRetryHistory() + "\n" +
                        buildRetryHistoryEntry(failedMessage.getRetryAttempts() + 1, 
                                              "MANUAL_FAILED", 
                                              "Manual retry by " + adminUser + " failed"));
                
                log.error("❌ [MANUAL RETRY FAILED] Message ID: {} failed after manual retry", messageId);
            }
            
            failedMessageRepository.save(failedMessage);
            return success;
            
        } catch (Exception e) {
            log.error("❌ [MANUAL RETRY ERROR] Error during manual retry: {}", e.getMessage(), e);
            failedMessage.setStatus(RetryStatus.FAILED);
            failedMessageRepository.save(failedMessage);
            return false;
        }
    }
    
    /**
     * Simulates reprocessing of a failed registration.
     * TODO: Integrate with actual registration flow
     * 
     * @param registrationDTO The registration data
     * @param failureStage The stage that previously failed
     * @return true if reprocessing succeeded
     */
    private boolean attemptReprocessing(StudentRegistrationDTO registrationDTO, String failureStage) {
        // TODO: Implement actual reprocessing logic based on failure stage
        // For now, simulate success for demonstration purposes
        // In production, this would call the appropriate service methods
        
        log.info("🔄 [REPROCESSING] Attempting to reprocess registration for: {} at stage: {}", 
                 registrationDTO.getStudentId(), failureStage);
        
        // Simulate processing (80% success rate for demonstration)
        boolean success = Math.random() > 0.2;
        
        if (success) {
            log.info("✅ [REPROCESSING] Successfully reprocessed registration");
        } else {
            log.warn("⚠️ [REPROCESSING] Reprocessing failed (simulated failure)");
        }
        
        return success;
    }
    
    /**
     * Builds a retry history entry with timestamp.
     */
    private String buildRetryHistoryEntry(int attemptNumber, String result, String details) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return String.format("[%s] Attempt %d: %s - %s", 
                           now.format(formatter), 
                           attemptNumber, 
                           result, 
                           details);
    }
    
    /**
     * Extracts stack trace from exception.
     */
    private String getStackTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();
        
        // Truncate if too long (max 5000 chars for database)
        if (stackTrace.length() > 5000) {
            stackTrace = stackTrace.substring(0, 4900) + "... (truncated)";
        }
        
        return stackTrace;
    }
    
    /**
     * Get all failed messages in DLQ.
     */
    public List<FailedMessage> getDeadLetterQueue() {
        return failedMessageRepository.findByInDeadLetterQueueTrue();
    }
    
    /**
     * Get all messages by status.
     */
    public List<FailedMessage> getMessagesByStatus(RetryStatus status) {
        return failedMessageRepository.findByStatus(status);
    }
    
    /**
     * Get unresolved messages.
     */
    public List<FailedMessage> getUnresolvedMessages() {
        List<RetryStatus> finalStatuses = Arrays.asList(
            RetryStatus.RETRY_SUCCESS, 
            RetryStatus.MANUAL_RETRY_SUCCESS, 
            RetryStatus.FAILED
        );
        return failedMessageRepository.findUnresolvedMessages(finalStatuses);
    }
}
