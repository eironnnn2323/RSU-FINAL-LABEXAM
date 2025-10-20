package com.rsu.registration.translator.impl;

import com.rsu.registration.dto.AcademicRecordsXmlDto;
import com.rsu.registration.dto.BillingSystemJsonDto;
import com.rsu.registration.translator.MessageTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Message Translator: XML → JSON
 * Translates Academic Records XML format to Billing System JSON format
 */
@Component
public class XmlToJsonTranslator implements MessageTranslator<AcademicRecordsXmlDto, BillingSystemJsonDto> {
    
    private static final Logger logger = LoggerFactory.getLogger(XmlToJsonTranslator.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Tuition fee mapping by program
    private static final Map<String, Double> TUITION_FEES = new HashMap<>();
    
    static {
        TUITION_FEES.put("Computer Science", 45000.00);
        TUITION_FEES.put("Engineering", 50000.00);
        TUITION_FEES.put("Business Administration", 40000.00);
        TUITION_FEES.put("Nursing", 48000.00);
        TUITION_FEES.put("Education", 35000.00);
        TUITION_FEES.put("Liberal Arts", 38000.00);
    }

    @Override
    public BillingSystemJsonDto translate(AcademicRecordsXmlDto source) {
        logger.info("🔄 [MESSAGE TRANSLATOR] Starting XML → JSON translation for student: {}", source.getStudentId());
        
        try {
            String timestamp = LocalDateTime.now().format(formatter);
            Double tuitionFee = TUITION_FEES.getOrDefault(source.getProgram(), 40000.00);
            
            BillingSystemJsonDto jsonDto = new BillingSystemJsonDto(
                    source.getStudentId(),
                    source.getStudentName(),
                    source.getEmail(),
                    source.getProgram(),
                    source.getYearLevel(),
                    timestamp,
                    tuitionFee,
                    "PENDING"
            );
            
            logger.info("✅ [MESSAGE TRANSLATOR] Successfully translated XML → JSON");
            logger.debug("   Source (XML):  {}", source);
            logger.debug("   Target (JSON): {}", jsonDto);
            logger.debug("   Tuition Fee Calculated: ₱{}", tuitionFee);
            
            return jsonDto;
            
        } catch (Exception e) {
            logger.error("❌ [MESSAGE TRANSLATOR] Failed to translate XML → JSON: {}", e.getMessage());
            throw new RuntimeException("Translation failed: XML → JSON", e);
        }
    }

    @Override
    public String getTranslatorName() {
        return "XML to JSON Translator (Billing System)";
    }

    @Override
    public String getSourceFormat() {
        return "XML";
    }

    @Override
    public String getTargetFormat() {
        return "JSON";
    }
}
