package com.rsu.registration.model;

/**
 * Categories of errors that can occur during registration process.
 * Used for filtering and analytics in admin dashboard.
 */
public enum ErrorCategory {
    /**
     * System or service is temporarily unavailable
     */
    SYSTEM_DOWN,
    
    /**
     * Network communication failure
     */
    NETWORK_TIMEOUT,
    
    /**
     * Invalid data format or validation error
     */
    INVALID_DATA,
    
    /**
     * Database operation failed
     */
    DATABASE_ERROR,
    
    /**
     * Message queue operation failed
     */
    QUEUE_ERROR,
    
    /**
     * Message transformation/translation failed
     */
    TRANSLATION_ERROR,
    
    /**
     * Routing logic failed
     */
    ROUTING_ERROR,
    
    /**
     * Aggregation process failed
     */
    AGGREGATION_ERROR,
    
    /**
     * Unknown or uncategorized error
     */
    UNKNOWN
}
