package com.rsu.registration.model;

/**
 * Status of a failed message in the retry system.
 * Tracks the current state of error recovery process.
 */
public enum RetryStatus {
    /**
     * Message failed and is pending first retry
     */
    PENDING_RETRY,
    
    /**
     * Currently attempting to retry the message
     */
    RETRYING,
    
    /**
     * Retry was successful and message was processed
     */
    RETRY_SUCCESS,
    
    /**
     * All retry attempts exhausted, moved to dead-letter queue
     */
    MOVED_TO_DLQ,
    
    /**
     * Awaiting manual intervention from admin
     */
    AWAITING_MANUAL_RETRY,
    
    /**
     * Admin manually retried and succeeded
     */
    MANUAL_RETRY_SUCCESS,
    
    /**
     * Message permanently failed even after manual retry
     */
    FAILED
}
