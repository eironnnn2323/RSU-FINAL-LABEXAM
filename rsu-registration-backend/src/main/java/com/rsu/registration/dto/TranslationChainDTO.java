package com.rsu.registration.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO to track message translation chain
 * Shows how messages are translated through different formats
 */
public class TranslationChainDTO {
    
    private String originalFormat;
    private List<TranslationStep> translationSteps;
    private long totalTranslationTimeMs;
    private boolean successful;

    public TranslationChainDTO() {
        this.translationSteps = new ArrayList<>();
        this.successful = true;
    }

    public void addTranslationStep(String translatorName, String fromFormat, String toFormat, 
                                   Object sourceData, Object targetData, long durationMs) {
        TranslationStep step = new TranslationStep();
        step.setTranslatorName(translatorName);
        step.setFromFormat(fromFormat);
        step.setToFormat(toFormat);
        step.setSourceData(sourceData != null ? sourceData.toString() : "null");
        step.setTargetData(targetData != null ? targetData.toString() : "null");
        step.setDurationMs(durationMs);
        step.setTimestamp(java.time.LocalDateTime.now().toString());
        
        this.translationSteps.add(step);
        this.totalTranslationTimeMs += durationMs;
    }

    // Getters and Setters
    public String getOriginalFormat() {
        return originalFormat;
    }

    public void setOriginalFormat(String originalFormat) {
        this.originalFormat = originalFormat;
    }

    public List<TranslationStep> getTranslationSteps() {
        return translationSteps;
    }

    public void setTranslationSteps(List<TranslationStep> translationSteps) {
        this.translationSteps = translationSteps;
    }

    public long getTotalTranslationTimeMs() {
        return totalTranslationTimeMs;
    }

    public void setTotalTranslationTimeMs(long totalTranslationTimeMs) {
        this.totalTranslationTimeMs = totalTranslationTimeMs;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    // Inner class for translation steps
    public static class TranslationStep {
        private String translatorName;
        private String fromFormat;
        private String toFormat;
        private String sourceData;
        private String targetData;
        private long durationMs;
        private String timestamp;

        // Getters and Setters
        public String getTranslatorName() {
            return translatorName;
        }

        public void setTranslatorName(String translatorName) {
            this.translatorName = translatorName;
        }

        public String getFromFormat() {
            return fromFormat;
        }

        public void setFromFormat(String fromFormat) {
            this.fromFormat = fromFormat;
        }

        public String getToFormat() {
            return toFormat;
        }

        public void setToFormat(String toFormat) {
            this.toFormat = toFormat;
        }

        public String getSourceData() {
            return sourceData;
        }

        public void setSourceData(String sourceData) {
            this.sourceData = sourceData;
        }

        public String getTargetData() {
            return targetData;
        }

        public void setTargetData(String targetData) {
            this.targetData = targetData;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(long durationMs) {
            this.durationMs = durationMs;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
