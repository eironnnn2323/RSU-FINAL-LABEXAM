package com.rsu.registration.service;

import com.rsu.registration.dto.*;
import com.rsu.registration.translator.impl.JsonToXmlTranslator;
import com.rsu.registration.translator.impl.XmlToCsvTranslator;
import com.rsu.registration.translator.impl.XmlToJsonTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Message Translator Service
 * Coordinates message translations between different formats
 */
@Service
public class MessageTranslatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageTranslatorService.class);
    
    @Autowired
    private JsonToXmlTranslator jsonToXmlTranslator;
    
    @Autowired
    private XmlToJsonTranslator xmlToJsonTranslator;
    
    @Autowired
    private XmlToCsvTranslator xmlToCsvTranslator;

    /**
     * Execute complete translation chain for student registration
     * JSON → XML → JSON (Billing) & CSV (Library)
     */
    public TranslationChainDTO executeTranslationChain(StudentRegistrationDTO registration) {
        logger.info("🔄 [TRANSLATION CHAIN] Starting translation chain for student: {}", registration.getStudentId());
        
        TranslationChainDTO chain = new TranslationChainDTO();
        chain.setOriginalFormat("JSON");
        
        try {
            // Step 1: JSON → XML (for Academic Records)
            long startTime = System.currentTimeMillis();
            AcademicRecordsXmlDto xmlDto = jsonToXmlTranslator.translate(registration);
            long duration = System.currentTimeMillis() - startTime;
            
            chain.addTranslationStep(
                    jsonToXmlTranslator.getTranslatorName(),
                    jsonToXmlTranslator.getSourceFormat(),
                    jsonToXmlTranslator.getTargetFormat(),
                    registration,
                    xmlDto,
                    duration
            );
            logger.info("   ✅ Step 1: JSON → XML completed in {}ms", duration);
            
            // Step 2: XML → JSON (for Billing System)
            startTime = System.currentTimeMillis();
            BillingSystemJsonDto billingJsonDto = xmlToJsonTranslator.translate(xmlDto);
            duration = System.currentTimeMillis() - startTime;
            
            chain.addTranslationStep(
                    xmlToJsonTranslator.getTranslatorName(),
                    xmlToJsonTranslator.getSourceFormat(),
                    xmlToJsonTranslator.getTargetFormat(),
                    xmlDto,
                    billingJsonDto,
                    duration
            );
            logger.info("   ✅ Step 2: XML → JSON (Billing) completed in {}ms", duration);
            
            // Step 3: XML → CSV (for Library Services)
            startTime = System.currentTimeMillis();
            LibraryServicesCsvDto csvDto = xmlToCsvTranslator.translate(xmlDto);
            duration = System.currentTimeMillis() - startTime;
            
            chain.addTranslationStep(
                    xmlToCsvTranslator.getTranslatorName(),
                    xmlToCsvTranslator.getSourceFormat(),
                    xmlToCsvTranslator.getTargetFormat(),
                    xmlDto,
                    csvDto,
                    duration
            );
            logger.info("   ✅ Step 3: XML → CSV (Library) completed in {}ms", duration);
            
            chain.setSuccessful(true);
            logger.info("✅ [TRANSLATION CHAIN] Complete! Total time: {}ms", chain.getTotalTranslationTimeMs());
            
        } catch (Exception e) {
            chain.setSuccessful(false);
            logger.error("❌ [TRANSLATION CHAIN] Failed: {}", e.getMessage());
            throw new RuntimeException("Translation chain failed", e);
        }
        
        return chain;
    }

    /**
     * Translate JSON to XML for Academic Records
     */
    public AcademicRecordsXmlDto translateToXml(StudentRegistrationDTO registration) {
        logger.info("🔄 Translating to XML for student: {}", registration.getStudentId());
        return jsonToXmlTranslator.translate(registration);
    }

    /**
     * Translate XML to JSON for Billing System
     */
    public BillingSystemJsonDto translateToBillingJson(AcademicRecordsXmlDto xmlDto) {
        logger.info("🔄 Translating to Billing JSON for student: {}", xmlDto.getStudentId());
        return xmlToJsonTranslator.translate(xmlDto);
    }

    /**
     * Translate XML to CSV for Library Services
     */
    public LibraryServicesCsvDto translateToCsv(AcademicRecordsXmlDto xmlDto) {
        logger.info("🔄 Translating to CSV for student: {}", xmlDto.getStudentId());
        return xmlToCsvTranslator.translate(xmlDto);
    }

    /**
     * Get translation chain information without executing translations
     */
    public String getTranslationChainInfo() {
        return String.format(
                "Translation Chain: %s → %s → [%s, %s]",
                jsonToXmlTranslator.getSourceFormat(),
                jsonToXmlTranslator.getTargetFormat(),
                xmlToJsonTranslator.getTargetFormat(),
                xmlToCsvTranslator.getTargetFormat()
        );
    }
}
