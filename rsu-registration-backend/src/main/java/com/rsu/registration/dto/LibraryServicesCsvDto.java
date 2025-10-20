package com.rsu.registration.dto;

/**
 * CSV format for Library Services
 * Message Translator Pattern - CSV representation
 */
public class LibraryServicesCsvDto {
    
    private String csvData;
    private String format = "CSV";
    
    // Individual fields for easy access
    private String studentId;
    private String name;
    private String email;
    private String program;
    private String yearLevel;
    private String registrationDate;

    public LibraryServicesCsvDto() {
    }

    public LibraryServicesCsvDto(String studentId, String name, String email,
                                  String program, String yearLevel, String registrationDate) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.program = program;
        this.yearLevel = yearLevel;
        this.registrationDate = registrationDate;
        this.csvData = buildCsvString();
    }

    private String buildCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s",
                escapeCSV(studentId),
                escapeCSV(name),
                escapeCSV(email),
                escapeCSV(program),
                escapeCSV(yearLevel),
                escapeCSV(registrationDate));
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static String getCsvHeader() {
        return "StudentID,Name,Email,Program,YearLevel,RegistrationDate";
    }

    // Getters and Setters
    public String getCsvData() {
        return csvData;
    }

    public void setCsvData(String csvData) {
        this.csvData = csvData;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
        this.csvData = buildCsvString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.csvData = buildCsvString();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.csvData = buildCsvString();
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
        this.csvData = buildCsvString();
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
        this.csvData = buildCsvString();
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
        this.csvData = buildCsvString();
    }

    @Override
    public String toString() {
        return "LibraryServicesCsvDto{" +
                "csvData='" + csvData + '\'' +
                ", format='" + format + '\'' +
                '}';
    }
}
