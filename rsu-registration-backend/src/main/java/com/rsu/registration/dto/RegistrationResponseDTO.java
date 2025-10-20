package com.rsu.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Registration API
 * Includes routing information for content-based routing
 * and translation chain information for Message Translator pattern
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponseDTO {

    private boolean success;
    private String message;
    private Long registrationId;
    private String status;
    
    // Content-Based Routing information
    private boolean isFirstYear;
    private List<String> routedTo;
    private String routingMessage;
    
    // Message Translator Pattern information
    private TranslationChainDTO translationChain;
}
