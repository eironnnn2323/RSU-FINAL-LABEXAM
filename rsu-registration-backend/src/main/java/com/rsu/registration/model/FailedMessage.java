package com.rsu.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a failed message in the error channel.
 * Stores message details, error information, and retry metadata.
 * 
 * Used for:
 * - Tracking failed registrations
 * - Managing retry attempts with exponential backoff
 * - Dead-letter queue management
 * - Admin dashboard display
 */
@Entity
@Table(name = "failed_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Original student ID from registration
     */
    @Column(nullable = false)
    private String studentId;
    
    /**
     * Student name for display purposes
     */
    @Column(nullable = false)
    private String studentName;
    
    /**
     * Email for notifications
     */
    private String email;
    
    /**
     * Original registration data as JSON
     */
    @Column(length = 2000)
    private String originalMessage;
    
    /**
     * Stage where failure occurred (e.g., "ROUTING", "HOUSING_SERVICE", "AGGREGATION")
     */
    private String failureStage;
    
    /**
     * Category of error for filtering
     */
    @Enumerated(EnumType.ORDINAL)
    private ErrorCategory errorCategory;
    
    /**
     * Detailed error message
     */
    @Column(length = 1000)
    private String errorMessage;
    
    /**
     * Full stack trace for debugging
     */
    @Column(length = 5000)
    private String stackTrace;
    
    /**
     * Current status in retry system
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private RetryStatus status;
    
    /**
     * Number of retry attempts made (max 3)
     */
    private int retryAttempts;
    
    /**
     * Maximum retry attempts allowed
     */
    private int maxRetryAttempts;
    
    /**
     * When the message first failed
     */
    private LocalDateTime failedAt;
    
    /**
     * When the next retry is scheduled
     */
    private LocalDateTime nextRetryAt;
    
    /**
     * When the last retry attempt was made
     */
    private LocalDateTime lastRetryAt;
    
    /**
     * When message was moved to dead-letter queue
     */
    private LocalDateTime movedToDlqAt;
    
    /**
     * When message was successfully processed (after retry or manual intervention)
     */
    private LocalDateTime resolvedAt;
    
    /**
     * Admin who manually retried (if applicable)
     */
    private String retriedBy;
    
    /**
     * Admin notes for manual intervention
     */
    private String adminNotes;
    
    /**
     * History of all retry attempts with timestamps
     */
    @Column(length = 2000)
    private String retryHistory;
    
    /**
     * Indicates if message is in dead-letter queue
     */
    @Column(nullable = false)
    private boolean inDeadLetterQueue;
    
    /**
     * Indicates if user has been notified of failure
     */
    @Column(nullable = false)
    private boolean userNotified;
    
    /**
     * Last notification sent to user
     */
    private LocalDateTime lastNotificationAt;
    
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = RetryStatus.PENDING_RETRY;
        }
        if (failedAt == null) {
            failedAt = LocalDateTime.now();
        }
        if (maxRetryAttempts == 0) {
            maxRetryAttempts = 3;
        }
    }
}
