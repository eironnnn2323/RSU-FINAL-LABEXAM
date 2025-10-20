package com.rsu.registration.translator.impl;

import com.rsu.registration.dto.AcademicRecordsXmlDto;
import com.rsu.registration.dto.LibraryServicesCsvDto;
import com.rsu.registration.translator.MessageTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message Translator: XML → CSV
 * Translates Academic Records XML format to Library Services CSV format
 */
@Component
public class XmlToCsvTranslator implements MessageTranslator<AcademicRecordsXmlDto, LibraryServicesCsvDto> {
    
    private static final Logger logger = LoggerFactory.getLogger(XmlToCsvTranslator.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LibraryServicesCsvDto translate(AcademicRecordsXmlDto source) {
        logger.info("🔄 [MESSAGE TRANSLATOR] Starting XML → CSV translation for student: {}", source.getStudentId());
        
        try {
            String timestamp = LocalDateTime.now().format(formatter);
            
            LibraryServicesCsvDto csvDto = new LibraryServicesCsvDto(
                    source.getStudentId(),
                    source.getStudentName(),
                    source.getEmail(),
                    source.getProgram(),
                    source.getYearLevel(),
                    timestamp
            );
            
            logger.info("✅ [MESSAGE TRANSLATOR] Successfully translated XML → CSV");
            logger.debug("   Source (XML): {}", source);
            logger.debug("   Target (CSV): {}", csvDto.getCsvData());
            logger.debug("   CSV Header:   {}", LibraryServicesCsvDto.getCsvHeader());
            
            return csvDto;
            
        } catch (Exception e) {
            logger.error("❌ [MESSAGE TRANSLATOR] Failed to translate XML → CSV: {}", e.getMessage());
            throw new RuntimeException("Translation failed: XML → CSV", e);
        }
    }

    @Override
    public String getTranslatorName() {
        return "XML to CSV Translator (Library Services)";
    }

    @Override
    public String getSourceFormat() {
        return "XML";
    }

    @Override
    public String getTargetFormat() {
        return "CSV";
    }
}
