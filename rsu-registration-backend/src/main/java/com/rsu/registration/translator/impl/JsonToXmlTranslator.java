package com.rsu.registration.translator.impl;

import com.rsu.registration.dto.AcademicRecordsXmlDto;
import com.rsu.registration.dto.StudentRegistrationDTO;
import com.rsu.registration.translator.MessageTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message Translator: JSON → XML
 * Translates frontend JSON format to Academic Records XML format
 */
@Component
public class JsonToXmlTranslator implements MessageTranslator<StudentRegistrationDTO, AcademicRecordsXmlDto> {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonToXmlTranslator.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public AcademicRecordsXmlDto translate(StudentRegistrationDTO source) {
        logger.info("🔄 [MESSAGE TRANSLATOR] Starting JSON → XML translation for student: {}", source.getStudentId());
        
        try {
            String timestamp = LocalDateTime.now().format(formatter);
            
            AcademicRecordsXmlDto xmlDto = new AcademicRecordsXmlDto(
                    source.getStudentName(),
                    source.getStudentId(),
                    source.getEmail(),
                    source.getProgram(),
                    source.getYearLevel(),
                    timestamp
            );
            
            logger.info("✅ [MESSAGE TRANSLATOR] Successfully translated JSON → XML");
            logger.debug("   Source (JSON): {}", source);
            logger.debug("   Target (XML):  {}", xmlDto);
            
            return xmlDto;
            
        } catch (Exception e) {
            logger.error("❌ [MESSAGE TRANSLATOR] Failed to translate JSON → XML: {}", e.getMessage());
            throw new RuntimeException("Translation failed: JSON → XML", e);
        }
    }

    @Override
    public String getTranslatorName() {
        return "JSON to XML Translator (Academic Records)";
    }

    @Override
    public String getSourceFormat() {
        return "JSON";
    }

    @Override
    public String getTargetFormat() {
        return "XML";
    }
}
