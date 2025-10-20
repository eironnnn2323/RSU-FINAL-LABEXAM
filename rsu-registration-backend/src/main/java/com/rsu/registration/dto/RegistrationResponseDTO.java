package com.rsu.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Registration API
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
}
