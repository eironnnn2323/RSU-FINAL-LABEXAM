package com.rsu.registration.controller;

import com.rsu.registration.model.ErrorCategory;
import com.rsu.registration.model.ErrorLog;
import com.rsu.registration.model.FailedMessage;
import com.rsu.registration.model.RetryStatus;
import com.rsu.registration.service.ErrorLogService;
import com.rsu.registration.service.RetryService;
import com.rsu.registration.repository.FailedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Dashboard REST API Controller
 * Provides endpoints for error monitoring, failed message management,
 * manual retry operations, and system metrics.
 * 
 * Features:
 * - View all error logs with filtering
 * - View failed registrations in error channel and DLQ
 * - Manual retry failed messages
 * - System health and error statistics
 * - Search and filter capabilities
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = "*", allowCredentials = "false", maxAge = 3600)
public class AdminController {
    
    private final RetryService retryService;
    private final ErrorLogService errorLogService;
    private final FailedMessageRepository failedMessageRepository;
    
    // ========== Error Logs Endpoints ==========
    
    /**
     * Get all error logs (recent 100)
     */
    @GetMapping("/errors")
    public ResponseEntity<List<ErrorLog>> getAllErrors() {
        log.info("📊 [ADMIN] Fetching all error logs");
        List<ErrorLog> errors = errorLogService.getRecentErrors();
        return ResponseEntity.ok(errors);
    }
    
    /**
     * Get errors by category
     */
    @GetMapping("/errors/category/{category}")
    public ResponseEntity<List<ErrorLog>> getErrorsByCategory(@PathVariable ErrorCategory category) {
        log.info("📊 [ADMIN] Fetching errors for category: {}", category);
        List<ErrorLog> errors = errorLogService.getErrorsByCategory(category);
        return ResponseEntity.ok(errors);
    }
    
    /**
     * Get errors by stage
     */
    @GetMapping("/errors/stage/{stage}")
    public ResponseEntity<List<ErrorLog>> getErrorsByStage(@PathVariable String stage) {
        log.info("📊 [ADMIN] Fetching errors for stage: {}", stage);
        List<ErrorLog> errors = errorLogService.getErrorsByStage(stage);
        return ResponseEntity.ok(errors);
    }
    
    /**
     * Get unresolved errors
     */
    @GetMapping("/errors/unresolved")
    public ResponseEntity<List<ErrorLog>> getUnresolvedErrors() {
        log.info("📊 [ADMIN] Fetching unresolved errors");
        List<ErrorLog> errors = errorLogService.getUnresolvedErrors();
        return ResponseEntity.ok(errors);
    }
    
    /**
     * Search errors with multiple criteria
     */
    @GetMapping("/errors/search")
    public ResponseEntity<List<ErrorLog>> searchErrors(
            @RequestParam(required = false) ErrorCategory category,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(required = false) String studentId) {
        
        log.info("🔍 [ADMIN] Searching errors with filters - Category: {}, Stage: {}, Severity: {}, Resolved: {}, StudentID: {}", 
                 category, stage, severity, resolved, studentId);
        
        List<ErrorLog> errors = errorLogService.searchErrors(category, stage, severity, resolved, studentId);
        return ResponseEntity.ok(errors);
    }
    
    // ========== Failed Messages Endpoints ==========
    
    /**
     * Get all failed messages
     */
    @GetMapping("/failed-messages")
    public ResponseEntity<List<FailedMessage>> getAllFailedMessages() {
        log.info("📊 [ADMIN] Fetching all failed messages");
        List<FailedMessage> messages = failedMessageRepository.findAll();
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get failed messages by status
     */
    @GetMapping("/failed-messages/status/{status}")
    public ResponseEntity<List<FailedMessage>> getFailedMessagesByStatus(@PathVariable RetryStatus status) {
        log.info("📊 [ADMIN] Fetching failed messages with status: {}", status);
        List<FailedMessage> messages = retryService.getMessagesByStatus(status);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get messages in Dead-Letter Queue
     */
    @GetMapping("/failed-messages/dlq")
    public ResponseEntity<List<FailedMessage>> getDeadLetterQueue() {
        log.info("💀 [ADMIN] Fetching Dead-Letter Queue messages");
        List<FailedMessage> messages = retryService.getDeadLetterQueue();
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get unresolved failed messages
     */
    @GetMapping("/failed-messages/unresolved")
    public ResponseEntity<List<FailedMessage>> getUnresolvedMessages() {
        log.info("📊 [ADMIN] Fetching unresolved failed messages");
        List<FailedMessage> messages = retryService.getUnresolvedMessages();
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get failed message by ID
     */
    @GetMapping("/failed-messages/{id}")
    public ResponseEntity<FailedMessage> getFailedMessageById(@PathVariable Long id) {
        log.info("📊 [ADMIN] Fetching failed message with ID: {}", id);
        return failedMessageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get failed messages by student ID
     */
    @GetMapping("/failed-messages/student/{studentId}")
    public ResponseEntity<FailedMessage> getFailedMessageByStudentId(@PathVariable String studentId) {
        log.info("📊 [ADMIN] Fetching failed message for student: {}", studentId);
        return failedMessageRepository.findByStudentId(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // ========== Manual Retry Operations ==========
    
    /**
     * Manually retry a failed message
     */
    @PostMapping("/retry/{messageId}")
    public ResponseEntity<Map<String, Object>> manualRetry(
            @PathVariable Long messageId,
            @RequestParam String adminUser,
            @RequestParam(required = false) String notes) {
        
        log.info("👤 [ADMIN] Manual retry initiated by: {} for message ID: {}", adminUser, messageId);
        
        try {
            boolean success = retryService.manualRetry(messageId, adminUser, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("messageId", messageId);
            response.put("retriedBy", adminUser);
            
            if (success) {
                response.put("message", "Manual retry completed successfully");
                log.info("✅ [ADMIN] Manual retry successful for message ID: {}", messageId);
            } else {
                response.put("message", "Manual retry failed - message still cannot be processed");
                log.warn("⚠️ [ADMIN] Manual retry failed for message ID: {}", messageId);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [ADMIN] Error during manual retry: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error during manual retry: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Retry all messages in DLQ
     */
    @PostMapping("/retry-all-dlq")
    public ResponseEntity<Map<String, Object>> retryAllDlq(
            @RequestParam String adminUser,
            @RequestParam(required = false) String notes) {
        
        log.info("👤 [ADMIN] Retry all DLQ initiated by: {}", adminUser);
        
        List<FailedMessage> dlqMessages = retryService.getDeadLetterQueue();
        int totalMessages = dlqMessages.size();
        int successCount = 0;
        int failureCount = 0;
        
        for (FailedMessage message : dlqMessages) {
            try {
                boolean success = retryService.manualRetry(message.getId(), adminUser, 
                        "Bulk retry: " + (notes != null ? notes : "Manual intervention"));
                if (success) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                log.error("❌ [ADMIN] Error retrying message {}: {}", message.getId(), e.getMessage());
                failureCount++;
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalMessages", totalMessages);
        response.put("successCount", successCount);
        response.put("failureCount", failureCount);
        response.put("message", String.format("Processed %d messages: %d succeeded, %d failed", 
                                              totalMessages, successCount, failureCount));
        
        log.info("✅ [ADMIN] Bulk DLQ retry complete - Success: {}, Failed: {}", successCount, failureCount);
        
        return ResponseEntity.ok(response);
    }
    
    // ========== Statistics and Metrics ==========
    
    /**
     * Get comprehensive system statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {
        log.info("📊 [ADMIN] Fetching system statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Error log statistics
        stats.put("totalErrors", errorLogService.getUnresolvedErrorCount());
        stats.put("unresolvedErrors", errorLogService.getUnresolvedErrorCount());
        stats.put("errorsByCategory", errorLogService.getErrorStatisticsByCategory());
        stats.put("errorsByStage", errorLogService.getErrorStatisticsByStage());
        stats.put("errorsBySeverity", errorLogService.getErrorStatisticsBySeverity());
        
        // Failed message statistics
        stats.put("totalFailedMessages", failedMessageRepository.count());
        stats.put("messagesInDlq", failedMessageRepository.countByInDeadLetterQueueTrue());
        stats.put("pendingRetry", failedMessageRepository.countByStatus(RetryStatus.PENDING_RETRY));
        stats.put("retrying", failedMessageRepository.countByStatus(RetryStatus.RETRYING));
        stats.put("awaitingManualRetry", failedMessageRepository.countByStatus(RetryStatus.AWAITING_MANUAL_RETRY));
        stats.put("retrySuccess", failedMessageRepository.countByStatus(RetryStatus.RETRY_SUCCESS));
        stats.put("manualRetrySuccess", failedMessageRepository.countByStatus(RetryStatus.MANUAL_RETRY_SUCCESS));
        stats.put("failed", failedMessageRepository.countByStatus(RetryStatus.FAILED));
        
        // Category breakdown for failed messages
        Map<String, Long> failuresByCategory = new HashMap<>();
        for (ErrorCategory category : ErrorCategory.values()) {
            long count = failedMessageRepository.countByErrorCategory(category);
            if (count > 0) {
                failuresByCategory.put(category.name(), count);
            }
        }
        stats.put("failuresByCategory", failuresByCategory);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get failure statistics by category
     */
    @GetMapping("/stats/failures-by-category")
    public ResponseEntity<Map<String, Long>> getFailuresByCategory() {
        log.info("📊 [ADMIN] Fetching failure statistics by category");
        
        Map<String, Long> stats = new HashMap<>();
        for (ErrorCategory category : ErrorCategory.values()) {
            long count = failedMessageRepository.countByErrorCategory(category);
            stats.put(category.name(), count);
        }
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get failure statistics by stage
     */
    @GetMapping("/stats/failures-by-stage")
    public ResponseEntity<List<Object[]>> getFailuresByStage() {
        log.info("📊 [ADMIN] Fetching failure statistics by stage");
        List<Object[]> stats = failedMessageRepository.getFailureStatisticsByStage();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get retry success rate
     */
    @GetMapping("/stats/retry-success-rate")
    public ResponseEntity<Map<String, Object>> getRetrySuccessRate() {
        log.info("📊 [ADMIN] Calculating retry success rate");
        
        long totalMessages = failedMessageRepository.countTotalFailures();
        long resolvedMessages = failedMessageRepository.countResolvedMessages();
        long inDlq = failedMessageRepository.countByInDeadLetterQueueTrue();
        
        double successRate = totalMessages > 0 ? (double) resolvedMessages / totalMessages * 100 : 0;
        double dlqRate = totalMessages > 0 ? (double) inDlq / totalMessages * 100 : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", totalMessages);
        stats.put("resolvedMessages", resolvedMessages);
        stats.put("messagesInDlq", inDlq);
        stats.put("successRate", String.format("%.2f%%", successRate));
        stats.put("dlqRate", String.format("%.2f%%", dlqRate));
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Health check for admin dashboard
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> adminHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("adminDashboard", "operational");
        health.put("errorTracking", "active");
        health.put("retryScheduler", "running");
        return ResponseEntity.ok(health);
    }
}
