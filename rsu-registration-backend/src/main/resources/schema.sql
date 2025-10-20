-- ============================================================
-- RSU Student Registration System - Database Initialization
-- ============================================================
-- This script creates the necessary database objects for the
-- student registration system. It will be automatically executed
-- by Spring Boot (ddl-auto=update in application.properties)
-- ============================================================

-- Create student_registrations table
CREATE TABLE IF NOT EXISTS student_registrations (
    id BIGSERIAL PRIMARY KEY,
    student_name VARCHAR(255) NOT NULL,
    student_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    program VARCHAR(255) NOT NULL,
    year_level VARCHAR(255) NOT NULL,
    registration_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    message VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_student_id ON student_registrations(student_id);
CREATE INDEX IF NOT EXISTS idx_email ON student_registrations(email);
CREATE INDEX IF NOT EXISTS idx_status ON student_registrations(status);
CREATE INDEX IF NOT EXISTS idx_registration_timestamp ON student_registrations(registration_timestamp);

-- Create audit table (optional - for tracking changes)
CREATE TABLE IF NOT EXISTS registration_audit (
    id BIGSERIAL PRIMARY KEY,
    registration_id BIGINT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    changed_by VARCHAR(255),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (registration_id) REFERENCES student_registrations(id) ON DELETE CASCADE
);

-- Create index on audit table
CREATE INDEX IF NOT EXISTS idx_audit_registration_id ON registration_audit(registration_id);

-- Insert sample data (optional)
-- Uncomment below to add test data
/*
INSERT INTO student_registrations 
(student_name, student_id, email, program, year_level, status) 
VALUES 
('Sample Student 1', 'RSU000001', 'student1@rsu.edu', 'Computer Science', 'First Year', 'REGISTERED'),
('Sample Student 2', 'RSU000002', 'student2@rsu.edu', 'Engineering', 'Second Year', 'REGISTERED'),
('Sample Student 3', 'RSU000003', 'student3@rsu.edu', 'Business Administration', 'Third Year', 'REGISTERED');
*/

-- Verify table creation
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('student_registrations', 'registration_audit');
