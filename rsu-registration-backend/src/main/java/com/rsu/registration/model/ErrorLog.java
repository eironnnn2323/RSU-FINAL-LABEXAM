package com.rsu.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for logging all errors that occur in the system.
 * Provides audit trail and analytics capabilities.
 * 
 * Used for:
 * - Error analytics and reporting
 * - System health monitoring
 * - Debugging and troubleshooting
 * - Admin dashboard error viewer
 */
@Entity
@Table(name = "error_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Student ID if error is related to a registration
     */
    private String studentId;
    
    /**
     * Student name for display
     */
    private String studentName;
    
    /**
     * Stage/component where error occurred
     */
    @Column(nullable = false)
    private String errorStage;
    
    /**
     * Category of error
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private ErrorCategory errorCategory;
    
    /**
     * Error message
     */
    @Column(length = 1000, nullable = false)
    private String errorMessage;
    
    /**
     * Full stack trace
     */
    @Column(length = 5000)
    private String stackTrace;
    
    /**
     * HTTP status code if applicable
     */
    private Integer httpStatusCode;
    
    /**
     * Request URL if applicable
     */
    private String requestUrl;
    
    /**
     * Request method (GET, POST, etc)
     */
    private String requestMethod;
    
    /**
     * User agent or system that triggered the error
     */
    private String userAgent;
    
    /**
     * IP address of request source
     */
    private String ipAddress;
    
    /**
     * When the error occurred
     */
    @Column(nullable = false)
    private LocalDateTime errorTimestamp;
    
    /**
     * Severity level (INFO, WARN, ERROR, FATAL)
     */
    private String severity;
    
    /**
     * Whether error was resolved
     */
    @Column(nullable = false)
    private boolean resolved;
    
    /**
     * When error was resolved
     */
    private LocalDateTime resolvedAt;
    
    /**
     * Who resolved the error
     */
    private String resolvedBy;
    
    /**
     * Notes about resolution
     */
    @Column(length = 1000)
    private String resolutionNotes;
    
    @PrePersist
    protected void onCreate() {
        if (errorTimestamp == null) {
            errorTimestamp = LocalDateTime.now();
        }
        if (severity == null) {
            severity = "ERROR";
        }
    }
}
