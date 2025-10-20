package com.rsu.registration.repository;

import com.rsu.registration.model.ErrorCategory;
import com.rsu.registration.model.FailedMessage;
import com.rsu.registration.model.RetryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing failed messages (error channel).
 * Provides CRUD operations and custom queries for error handling and retry logic.
 */
@Repository
public interface FailedMessageRepository extends JpaRepository<FailedMessage, Long> {
    
    /**
     * Find failed message by student ID
     */
    Optional<FailedMessage> findByStudentId(String studentId);
    
    /**
     * Find all messages by status
     */
    List<FailedMessage> findByStatus(RetryStatus status);
    
    /**
     * Find all messages in dead-letter queue
     */
    List<FailedMessage> findByInDeadLetterQueueTrue();
    
    /**
     * Find all messages by error category
     */
    List<FailedMessage> findByErrorCategory(ErrorCategory errorCategory);
    
    /**
     * Find messages that need retry (pending retry and scheduled time has passed)
     */
    @Query("SELECT fm FROM FailedMessage fm WHERE fm.status = :status " +
           "AND fm.nextRetryAt <= :currentTime " +
           "AND fm.retryAttempts < fm.maxRetryAttempts")
    List<FailedMessage> findMessagesReadyForRetry(@Param("status") RetryStatus status,
                                                   @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find messages that should be moved to DLQ (exhausted retries)
     */
    @Query("SELECT fm FROM FailedMessage fm WHERE fm.status = :status " +
           "AND fm.retryAttempts >= fm.maxRetryAttempts " +
           "AND fm.inDeadLetterQueue = false")
    List<FailedMessage> findMessagesForDeadLetterQueue(@Param("status") RetryStatus status);
    
    /**
     * Find recent failed messages (last 24 hours)
     */
    @Query("SELECT fm FROM FailedMessage fm WHERE fm.failedAt >= :since ORDER BY fm.failedAt DESC")
    List<FailedMessage> findRecentFailures(@Param("since") LocalDateTime since);
    
    /**
     * Count messages by status
     */
    long countByStatus(RetryStatus status);
    
    /**
     * Count messages in DLQ
     */
    long countByInDeadLetterQueueTrue();
    
    /**
     * Count messages by error category
     */
    long countByErrorCategory(ErrorCategory errorCategory);
    
    /**
     * Find unresolved messages (not in final states)
     */
    @Query("SELECT fm FROM FailedMessage fm WHERE fm.status NOT IN :finalStatuses " +
           "ORDER BY fm.failedAt DESC")
    List<FailedMessage> findUnresolvedMessages(@Param("finalStatuses") List<RetryStatus> finalStatuses);
    
    /**
     * Find messages by failure stage
     */
    List<FailedMessage> findByFailureStage(String failureStage);
    
    /**
     * Find messages awaiting manual retry
     */
    List<FailedMessage> findByStatusOrderByFailedAtDesc(RetryStatus status);
    
    /**
     * Count total failed messages
     */
    @Query("SELECT COUNT(fm) FROM FailedMessage fm")
    long countTotalFailures();
    
    /**
     * Count resolved messages
     */
    @Query("SELECT COUNT(fm) FROM FailedMessage fm WHERE fm.resolvedAt IS NOT NULL")
    long countResolvedMessages();
    
    /**
     * Get failure rate statistics
     */
    @Query("SELECT fm.errorCategory, COUNT(fm) FROM FailedMessage fm " +
           "GROUP BY fm.errorCategory")
    List<Object[]> getFailureStatisticsByCategory();
    
    /**
     * Get failure rate by stage
     */
    @Query("SELECT fm.failureStage, COUNT(fm) FROM FailedMessage fm " +
           "GROUP BY fm.failureStage")
    List<Object[]> getFailureStatisticsByStage();
}
