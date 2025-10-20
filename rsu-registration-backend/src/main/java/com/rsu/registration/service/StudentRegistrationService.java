package com.rsu.registration.service;

import com.rsu.registration.dto.StudentRegistrationDTO;
import com.rsu.registration.model.StudentRegistration;
import com.rsu.registration.repository.StudentRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for handling student registration business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentRegistrationService {

    private final StudentRegistrationRepository registrationRepository;

    /**
     * Save student registration received from message queue
     */
    @Transactional
    public StudentRegistration saveRegistration(StudentRegistrationDTO registrationDTO) {
        log.info("Saving registration for student: {}", registrationDTO.getStudentId());

        StudentRegistration registration = StudentRegistration.builder()
                .studentName(registrationDTO.getStudentName())
                .studentId(registrationDTO.getStudentId())
                .email(registrationDTO.getEmail())
                .program(registrationDTO.getProgram())
                .yearLevel(registrationDTO.getYearLevel())
                .registrationTimestamp(LocalDateTime.now())
                .status("REGISTERED")
                .message("Successfully registered")
                .build();

        StudentRegistration saved = registrationRepository.save(registration);
        log.info("Registration saved with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Get registration by ID
     */
    public StudentRegistration getRegistrationById(Long id) {
        log.info("Retrieving registration with ID: {}", id);
        return registrationRepository.findById(id)
                .orElse(null);
    }

    /**
     * Get registration by Student ID
     */
    public StudentRegistration getRegistrationByStudentId(String studentId) {
        log.info("Retrieving registration for student ID: {}", studentId);
        return registrationRepository.findByStudentId(studentId)
                .orElse(null);
    }

    /**
     * Update registration status
     */
    @Transactional
    public StudentRegistration updateRegistrationStatus(Long id, String status, String message) {
        log.info("Updating registration {} status to: {}", id, status);

        return registrationRepository.findById(id)
                .map(registration -> {
                    registration.setStatus(status);
                    registration.setMessage(message);
                    return registrationRepository.save(registration);
                })
                .orElse(null);
    }

    /**
     * Get total registrations count
     */
    public long getRegistrationCount() {
        return registrationRepository.count();
    }
}
