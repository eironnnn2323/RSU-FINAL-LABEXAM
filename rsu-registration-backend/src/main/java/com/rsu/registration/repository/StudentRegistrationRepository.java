package com.rsu.registration.repository;

import com.rsu.registration.model.StudentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for StudentRegistration entity
 */
@Repository
public interface StudentRegistrationRepository extends JpaRepository<StudentRegistration, Long> {

    /**
     * Find a registration by student ID
     */
    Optional<StudentRegistration> findByStudentId(String studentId);
}
