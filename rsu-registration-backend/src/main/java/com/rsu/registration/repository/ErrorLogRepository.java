package com.rsu.registration.repository;

import com.rsu.registration.model.ErrorCategory;
import com.rsu.registration.model.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for error logging and analytics.
 * Provides queries for error tracking, filtering, and reporting.
 */
@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    
    /**
     * Find all errors by student ID
     */
    List<ErrorLog> findByStudentIdOrderByErrorTimestampDesc(String studentId);
    
    /**
     * Find all errors by category
     */
    List<ErrorLog> findByErrorCategoryOrderByErrorTimestampDesc(ErrorCategory errorCategory);
    
    /**
     * Find all errors by stage
     */
    List<ErrorLog> findByErrorStageOrderByErrorTimestampDesc(String errorStage);
    
    /**
     * Find all errors by severity
     */
    List<ErrorLog> findBySeverityOrderByErrorTimestampDesc(String severity);
    
    /**
     * Find unresolved errors
     */
    List<ErrorLog> findByResolvedFalseOrderByErrorTimestampDesc();
    
    /**
     * Find errors within time range
     */
    @Query("SELECT el FROM ErrorLog el WHERE el.errorTimestamp >= :startTime " +
           "AND el.errorTimestamp <= :endTime ORDER BY el.errorTimestamp DESC")
    List<ErrorLog> findErrorsByTimeRange(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find recent errors (last N hours)
     */
    @Query("SELECT el FROM ErrorLog el WHERE el.errorTimestamp >= :since " +
           "ORDER BY el.errorTimestamp DESC")
    List<ErrorLog> findRecentErrors(@Param("since") LocalDateTime since);
    
    /**
     * Count errors by category
     */
    long countByErrorCategory(ErrorCategory errorCategory);
    
    /**
     * Count errors by severity
     */
    long countBySeverity(String severity);
    
    /**
     * Count unresolved errors
     */
    long countByResolvedFalse();
    
    /**
     * Get error statistics by category
     */
    @Query("SELECT el.errorCategory, COUNT(el) FROM ErrorLog el " +
           "GROUP BY el.errorCategory")
    List<Object[]> getErrorStatisticsByCategory();
    
    /**
     * Get error statistics by stage
     */
    @Query("SELECT el.errorStage, COUNT(el) FROM ErrorLog el " +
           "GROUP BY el.errorStage")
    List<Object[]> getErrorStatisticsByStage();
    
    /**
     * Get error statistics by severity
     */
    @Query("SELECT el.severity, COUNT(el) FROM ErrorLog el " +
           "GROUP BY el.severity")
    List<Object[]> getErrorStatisticsBySeverity();
    
    /**
     * Get error trend (count by hour for last 24 hours)
     */
    @Query("SELECT CAST(el.errorTimestamp AS date) as day, EXTRACT(HOUR FROM el.errorTimestamp) as hour, " +
           "COUNT(el) FROM ErrorLog el WHERE el.errorTimestamp >= :since " +
           "GROUP BY CAST(el.errorTimestamp AS date), EXTRACT(HOUR FROM el.errorTimestamp) " +
           "ORDER BY day, hour")
    List<Object[]> getErrorTrend(@Param("since") LocalDateTime since);
    
    /**
     * Find errors matching multiple criteria
     */
    @Query("SELECT el FROM ErrorLog el WHERE " +
           "(:category IS NULL OR el.errorCategory = :category) AND " +
           "(:stage IS NULL OR el.errorStage = :stage) AND " +
           "(:severity IS NULL OR el.severity = :severity) AND " +
           "(:resolved IS NULL OR el.resolved = :resolved) AND " +
           "(:studentId IS NULL OR el.studentId = :studentId) " +
           "ORDER BY el.errorTimestamp DESC")
    List<ErrorLog> findByMultipleCriteria(
        @Param("category") ErrorCategory category,
        @Param("stage") String stage,
        @Param("severity") String severity,
        @Param("resolved") Boolean resolved,
        @Param("studentId") String studentId
    );
    
    /**
     * Delete old resolved errors (cleanup)
     */
    @Query("DELETE FROM ErrorLog el WHERE el.resolved = true " +
           "AND el.resolvedAt < :before")
    void deleteOldResolvedErrors(@Param("before") LocalDateTime before);
}
