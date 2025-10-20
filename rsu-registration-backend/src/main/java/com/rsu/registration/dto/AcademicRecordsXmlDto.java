package com.rsu.registration.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * XML format for Academic Records System
 * Message Translator Pattern - XML representation
 */
@JacksonXmlRootElement(localName = "registration")
public class AcademicRecordsXmlDto {
    
    @JacksonXmlProperty(localName = "studentName")
    private String studentName;
    
    @JacksonXmlProperty(localName = "studentID")
    private String studentId;
    
    @JacksonXmlProperty(localName = "email")
    private String email;
    
    @JacksonXmlProperty(localName = "program")
    private String program;
    
    @JacksonXmlProperty(localName = "yearLevel")
    private String yearLevel;
    
    @JacksonXmlProperty(localName = "timestamp")
    private String timestamp;
    
    @JacksonXmlProperty(localName = "format")
    private String format = "XML";

    public AcademicRecordsXmlDto() {
    }

    public AcademicRecordsXmlDto(String studentName, String studentId, String email, 
                                  String program, String yearLevel, String timestamp) {
        this.studentName = studentName;
        this.studentId = studentId;
        this.email = email;
        this.program = program;
        this.yearLevel = yearLevel;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return "AcademicRecordsXmlDto{" +
                "studentName='" + studentName + '\'' +
                ", studentId='" + studentId + '\'' +
                ", email='" + email + '\'' +
                ", program='" + program + '\'' +
                ", yearLevel='" + yearLevel + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", format='" + format + '\'' +
                '}';
    }
}
