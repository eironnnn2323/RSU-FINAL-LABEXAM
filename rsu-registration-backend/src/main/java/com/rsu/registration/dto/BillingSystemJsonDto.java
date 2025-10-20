package com.rsu.registration.dto;

/**
 * JSON format for Billing System
 * Message Translator Pattern - Billing JSON representation
 */
public class BillingSystemJsonDto {
    
    private String studentIdentifier;
    private String fullName;
    private String emailAddress;
    private String academicProgram;
    private String currentYearLevel;
    private String registrationDate;
    private String format = "JSON";
    private Double tuitionFee;
    private String billingStatus;

    public BillingSystemJsonDto() {
    }

    public BillingSystemJsonDto(String studentIdentifier, String fullName, String emailAddress,
                                 String academicProgram, String currentYearLevel, 
                                 String registrationDate, Double tuitionFee, String billingStatus) {
        this.studentIdentifier = studentIdentifier;
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.academicProgram = academicProgram;
        this.currentYearLevel = currentYearLevel;
        this.registrationDate = registrationDate;
        this.tuitionFee = tuitionFee;
        this.billingStatus = billingStatus;
    }

    // Getters and Setters
    public String getStudentIdentifier() {
        return studentIdentifier;
    }

    public void setStudentIdentifier(String studentIdentifier) {
        this.studentIdentifier = studentIdentifier;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getAcademicProgram() {
        return academicProgram;
    }

    public void setAcademicProgram(String academicProgram) {
        this.academicProgram = academicProgram;
    }

    public String getCurrentYearLevel() {
        return currentYearLevel;
    }

    public void setCurrentYearLevel(String currentYearLevel) {
        this.currentYearLevel = currentYearLevel;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Double getTuitionFee() {
        return tuitionFee;
    }

    public void setTuitionFee(Double tuitionFee) {
        this.tuitionFee = tuitionFee;
    }

    public String getBillingStatus() {
        return billingStatus;
    }

    public void setBillingStatus(String billingStatus) {
        this.billingStatus = billingStatus;
    }

    @Override
    public String toString() {
        return "BillingSystemJsonDto{" +
                "studentIdentifier='" + studentIdentifier + '\'' +
                ", fullName='" + fullName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", academicProgram='" + academicProgram + '\'' +
                ", currentYearLevel='" + currentYearLevel + '\'' +
                ", registrationDate='" + registrationDate + '\'' +
                ", format='" + format + '\'' +
                ", tuitionFee=" + tuitionFee +
                ", billingStatus='" + billingStatus + '\'' +
                '}';
    }
}
