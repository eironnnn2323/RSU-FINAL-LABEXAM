package com.rsu.registration.service;

import com.rsu.registration.model.ErrorCategory;
import com.rsu.registration.model.ErrorLog;
import com.rsu.registration.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for comprehensive error logging and analytics.
 * Provides centralized error tracking for the entire registration system.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorLogService {
    
    private final ErrorLogRepository errorLogRepository;
    
    /**
     * Logs an error to the error log system.
     * 
     * @param studentId Student ID if error is registration-related
     * @param studentName Student name
     * @param errorStage Stage where error occurred
     * @param errorCategory Category of error
     * @param errorMessage Error message
     * @param stackTrace Full stack trace
     * @return The saved ErrorLog entity
     */
    @Transactional
    public ErrorLog logError(
            String studentId,
            String studentName,
            String errorStage,
            ErrorCategory errorCategory,
            String errorMessage,
            String stackTrace) {
        
        log.info("📝 [ERROR LOG] Logging error for student: {} at stage: {} - Category: {}", 
                 studentId, errorStage, errorCategory);
        
        ErrorLog errorLog = ErrorLog.builder()
                .studentId(studentId)
                .studentName(studentName)
                .errorStage(errorStage)
                .errorCategory(errorCategory)
                .errorMessage(errorMessage)
                .stackTrace(stackTrace)
                .errorTimestamp(LocalDateTime.now())
                .severity("ERROR")
                .resolved(false)
                .build();
        
        return errorLogRepository.save(errorLog);
    }
    
    /**
     * Logs an error with HTTP request details.
     */
    @Transactional
    public ErrorLog logError(
            String studentId,
            String studentName,
            String errorStage,
            ErrorCategory errorCategory,
            String errorMessage,
            String stackTrace,
            Integer httpStatusCode,
            String requestUrl,
            String requestMethod) {
        
        ErrorLog errorLog = ErrorLog.builder()
                .studentId(studentId)
                .studentName(studentName)
                .errorStage(errorStage)
                .errorCategory(errorCategory)
                .errorMessage(errorMessage)
                .stackTrace(stackTrace)
                .httpStatusCode(httpStatusCode)
                .requestUrl(requestUrl)
                .requestMethod(requestMethod)
                .errorTimestamp(LocalDateTime.now())
                .severity("ERROR")
                .resolved(false)
                .build();
        
        return errorLogRepository.save(errorLog);
    }
    
    /**
     * Marks an error as resolved.
     */
    @Transactional
    public void logResolution(String studentId, String errorStage, String resolutionNotes) {
        List<ErrorLog> unresolvedErrors = errorLogRepository
                .findByMultipleCriteria(null, errorStage, null, false, studentId);
        
        for (ErrorLog errorLog : unresolvedErrors) {
            errorLog.setResolved(true);
            errorLog.setResolvedAt(LocalDateTime.now());
            errorLog.setResolvedBy("SYSTEM");
            errorLog.setResolutionNotes(resolutionNotes);
            errorLogRepository.save(errorLog);
        }
        
        log.info("✅ [ERROR LOG] Marked {} errors as resolved for student: {}", 
                 unresolvedErrors.size(), studentId);
    }
    
    /**
     * Get recent errors (last 24 hours).
     */
    public List<ErrorLog> getRecentErrors() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return errorLogRepository.findRecentErrors(since);
    }
    
    /**
     * Get errors by category.
     */
    public List<ErrorLog> getErrorsByCategory(ErrorCategory category) {
        return errorLogRepository.findByErrorCategoryOrderByErrorTimestampDesc(category);
    }
    
    /**
     * Get errors by stage.
     */
    public List<ErrorLog> getErrorsByStage(String stage) {
        return errorLogRepository.findByErrorStageOrderByErrorTimestampDesc(stage);
    }
    
    /**
     * Get unresolved errors.
     */
    public List<ErrorLog> getUnresolvedErrors() {
        return errorLogRepository.findByResolvedFalseOrderByErrorTimestampDesc();
    }
    
    /**
     * Get error statistics by category.
     */
    public Map<String, Long> getErrorStatisticsByCategory() {
        List<Object[]> results = errorLogRepository.getErrorStatisticsByCategory();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> ((ErrorCategory) result[0]).name(),
                        result -> (Long) result[1]
                ));
    }
    
    /**
     * Get error statistics by stage.
     */
    public Map<String, Long> getErrorStatisticsByStage() {
        List<Object[]> results = errorLogRepository.getErrorStatisticsByStage();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }
    
    /**
     * Get error statistics by severity.
     */
    public Map<String, Long> getErrorStatisticsBySeverity() {
        List<Object[]> results = errorLogRepository.getErrorStatisticsBySeverity();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }
    
    /**
     * Get error count by category.
     */
    public long getErrorCount(ErrorCategory category) {
        return errorLogRepository.countByErrorCategory(category);
    }
    
    /**
     * Get unresolved error count.
     */
    public long getUnresolvedErrorCount() {
        return errorLogRepository.countByResolvedFalse();
    }
    
    /**
     * Search errors with multiple criteria.
     */
    public List<ErrorLog> searchErrors(
            ErrorCategory category,
            String stage,
            String severity,
            Boolean resolved,
            String studentId) {
        
        return errorLogRepository.findByMultipleCriteria(
                category, stage, severity, resolved, studentId);
    }
}
