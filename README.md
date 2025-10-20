# RSU Student Registration System - Enterprise Integration Patterns Lab

## Project Overview

This is a complete implementation of an **Enterprise Integration Patterns (EIP)** based student registration system for Rejoice State University. The system demonstrates real-world integration architecture using message-driven asynchronous processing.

## System Architecture

```
Frontend (React)
    ↓
API Gateway (Spring Boot REST Controller)
    ↓
Message Channel (RabbitMQ)
    ↓
Integration Layer (Spring Integration)
    ↓
Backend Service (Spring Boot Service)
    ↓
Database (PostgreSQL)
```

## Key Components

### 1. Frontend (React Application)
- **Location**: `rsu-registration-frontend/`
- **Purpose**: Provides a user-friendly web interface for student registration
- **Features**:
  - Dynamic form with validation
  - Real-time feedback messages
  - Responsive design
  - CORS-enabled communication with backend

### 2. Backend (Spring Boot Application)
- **Location**: `rsu-registration-backend/`
- **Purpose**: Handles business logic and integration

#### Key Classes:

**RegistrationApplication.java**
- Main Spring Boot entry point
- Enables component scanning for integration configuration

**RegistrationController.java** (API Gateway)
- REST endpoint: `POST /api/v1/registrations/submit`
- Receives registration data from frontend
- Sends data to message queue
- Returns immediate acknowledgment (asynchronous processing)

**RegistrationIntegrationConfig.java** (EIP Layer)
- Configures RabbitMQ exchange and queue
- Sets up Spring Integration message channels
- Defines message handlers using `@ServiceActivator`
- Implements the **Message Channel** EIP pattern

**StudentRegistrationService.java** (Service Layer)
- Persists registration data to database
- Provides repository methods
- Handles business logic

**StudentRegistration.java** (Entity)
- JPA entity mapped to database table
- Tracks registration metadata:
  - Student info (name, ID, email, program, year level)
  - Timestamp
  - Status (PENDING, REGISTERED, FAILED)

### 3. Message Broker (RabbitMQ)
- Provides asynchronous message queue
- Enables loose coupling between frontend and backend
- Queue name: `student.registration.queue`
- Exchange: `student.registration.exchange`

### 4. Database (PostgreSQL)
- Stores student registration records
- Table: `student_registrations`
- Provides durability and consistency

## Enterprise Integration Patterns Used

### 1. **Message Channel** Pattern
- Queue-based communication between API Gateway and Integration Layer
- Enables asynchronous, decoupled communication

### 2. **Message Handler** Pattern
- `@ServiceActivator` annotated method processes messages
- Transforms message payload into business action (database persistence)

### 3. **Inbound Adapter** Pattern
- `AmqpInboundChannelAdapter` listens to RabbitMQ queue
- Bridges external message system with Spring Integration

### 4. **Direct Channel** Pattern
- `registrationInputChannel` and `registrationServiceChannel`
- Routes messages synchronously within the integration layer

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- Node.js 14+ (for frontend)

### Setup Instructions

#### 1. Start Infrastructure (RabbitMQ & PostgreSQL)

```bash
cd docker
docker-compose up -d
```

Verify services are running:
```bash
docker-compose ps
```

Access RabbitMQ Management UI: http://localhost:15672 (guest/guest)

#### 2. Build Backend

```bash
cd rsu-registration-backend
mvn clean package
```

Or run directly:
```bash
mvn spring-boot:run
```

Backend runs on: http://localhost:8080

#### 3. Build Frontend

```bash
cd rsu-registration-frontend
npm install
npm start
```

Frontend runs on: http://localhost:3000

## Testing the System

### Test Scenario: Submit a Student Registration

#### Using the Web Interface
1. Navigate to http://localhost:3000
2. Fill in the registration form:
   - Name: "John Doe"
   - Student ID: "RSU123456"
   - Email: "john.doe@rsu.edu"
   - Program: "Computer Science"
   - Year Level: "First Year"
3. Click "Submit Registration"
4. Observe the success message

#### Using cURL
```bash
curl -X POST http://localhost:8080/api/v1/registrations/submit \
  -H "Content-Type: application/json" \
  -d '{
    "studentName": "Jane Smith",
    "studentId": "RSU789012",
    "email": "jane.smith@rsu.edu",
    "program": "Engineering",
    "yearLevel": "Second Year"
  }'
```

#### Expected Response
```json
{
  "success": true,
  "message": "Registration submitted successfully. Processing...",
  "registrationId": null,
  "status": "SUBMITTED"
}
```

### Verify Data in Database

Connect to PostgreSQL:
```bash
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration
```

Query registrations:
```sql
SELECT * FROM student_registrations;
SELECT COUNT(*) FROM student_registrations;
```

### Monitor RabbitMQ

Visit http://localhost:15672

- Username: guest
- Password: guest

View:
- Queues tab: `student.registration.queue`
- Message rates and pending messages

### Check Application Logs

```bash
# Backend logs
tail -f logs/spring.log

# Frontend development server
npm start  # Shows logs in terminal
```

## Data Flow Walkthrough

### Step 1: Frontend Submission
```javascript
POST http://localhost:8080/api/v1/registrations/submit
{
  "studentName": "John Doe",
  "studentId": "RSU123456",
  "email": "john.doe@rsu.edu",
  "program": "Computer Science",
  "yearLevel": "First Year"
}
```

### Step 2: API Gateway Processing (RegistrationController)
```java
@PostMapping("/submit")
public ResponseEntity<RegistrationResponseDTO> submitRegistration(
    @Valid @RequestBody StudentRegistrationDTO registrationDTO) {
    
    // Validate input
    // Send to message queue
    rabbitTemplate.convertAndSend(
        REGISTRATION_EXCHANGE,
        "student.registration.submit",
        registrationDTO
    );
    
    // Return acknowledgment
    return ResponseEntity.accepted().body(response);
}
```

### Step 3: Message Queue (RabbitMQ)
- Message stored in `student.registration.queue`
- Exchange routes message based on routing key

### Step 4: Integration Layer Processing
```java
@ServiceActivator(inputChannel = REGISTRATION_INPUT_CHANNEL)
public void processRegistration(@Payload StudentRegistrationDTO registrationDTO) {
    // Message received from queue
    var savedRegistration = registrationService.saveRegistration(registrationDTO);
}
```

### Step 5: Database Persistence
```java
@Transactional
public StudentRegistration saveRegistration(StudentRegistrationDTO registrationDTO) {
    StudentRegistration registration = StudentRegistration.builder()
        .studentName(registrationDTO.getStudentName())
        .studentId(registrationDTO.getStudentId())
        .email(registrationDTO.getEmail())
        .program(registrationDTO.getProgram())
        .yearLevel(registrationDTO.getYearLevel())
        .registrationTimestamp(LocalDateTime.now())
        .status("REGISTERED")
        .build();
    
    return registrationRepository.save(registration);
}
```

## API Endpoints

### Submit Registration
```
POST /api/v1/registrations/submit
Content-Type: application/json

Request Body:
{
  "studentName": "string",
  "studentId": "string",
  "email": "string",
  "program": "string",
  "yearLevel": "string"
}

Response:
{
  "success": boolean,
  "message": "string",
  "registrationId": number,
  "status": "string"
}
```

### Get Registration by ID
```
GET /api/v1/registrations/{id}

Response: StudentRegistration object
```

### Get Registration by Student ID
```
GET /api/v1/registrations/student/{studentId}

Response: StudentRegistration object
```

### Get Registration Count
```
GET /api/v1/registrations/stats/count

Response: Long (total count)
```

### Health Check
```
GET /api/v1/registrations/health

Response: "Registration service is running"
```

## Troubleshooting

### Issue: "Connection refused" errors
**Solution**: Ensure Docker containers are running
```bash
docker-compose ps
docker-compose logs
```

### Issue: Frontend cannot connect to backend
**Solution**: Check CORS configuration in RegistrationController and API_URL in RegistrationForm.js

### Issue: Messages not being processed
**Solution**: 
1. Check RabbitMQ connection settings in `application.properties`
2. Verify RabbitMQ logs: `docker-compose logs rabbitmq`
3. Check Spring Integration logs (enable debug logging)

### Issue: Database connection errors
**Solution**: 
1. Verify PostgreSQL is running: `docker-compose ps`
2. Check credentials in `application.properties`
3. Ensure database exists: 
```bash
docker exec -it rsu_postgres psql -U rsu_user -l
```

## Performance Testing

### Load Testing with cURL Loop
```bash
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/v1/registrations/submit \
    -H "Content-Type: application/json" \
    -d "{
      \"studentName\": \"Student $i\",
      \"studentId\": \"RSU$i\",
      \"email\": \"student$i@rsu.edu\",
      \"program\": \"Computer Science\",
      \"yearLevel\": \"First Year\"
    }" &
done
wait
```

### Verify All Registrations Stored
```bash
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT COUNT(*) FROM student_registrations;"
```

## Project Structure

```
RSU-FINAL-LABEXAM/
├── rsu-registration-backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/rsu/registration/
│       │   ├── RegistrationApplication.java
│       │   ├── controller/
│       │   │   └── RegistrationController.java
│       │   ├── integration/
│       │   │   └── RegistrationIntegrationConfig.java
│       │   ├── service/
│       │   │   └── StudentRegistrationService.java
│       │   ├── model/
│       │   │   └── StudentRegistration.java
│       │   ├── dto/
│       │   │   ├── StudentRegistrationDTO.java
│       │   │   └── RegistrationResponseDTO.java
│       │   └── repository/
│       │       └── StudentRegistrationRepository.java
│       └── resources/
│           └── application.properties
├── rsu-registration-frontend/
│   ├── package.json
│   ├── public/
│   │   └── index.html
│   └── src/
│       ├── index.js
│       ├── App.js
│       ├── App.css
│       ├── styles/
│       │   └── index.css
│       └── components/
│           ├── RegistrationForm.js
│           ├── RegistrationForm.css
│           ├── Header.js
│           └── Header.css
├── docker/
│   └── docker-compose.yml
└── README.md
```

## Learning Outcomes

After completing this lab, students will understand:

1. **Enterprise Integration Patterns** - Message channels, handlers, adapters
2. **Asynchronous Processing** - Message queues for decoupled systems
3. **Spring Integration Framework** - Building integration flows
4. **Message-Oriented Middleware** - RabbitMQ fundamentals
5. **REST API Design** - Accepting async requests
6. **Frontend-Backend Integration** - CORS, API consumption
7. **Database Persistence** - JPA/Hibernate with Spring Data
8. **System Architecture** - Multi-layer application design
9. **Real-world Integration** - Connecting disparate systems

## Checkpoint Verification

### Checkpoint: Basic Integration Setup

#### ✓ Requirement 1: Web Registration Form
- Form accepts: Student Name, ID, Email, Program, Year Level
- Form includes validation
- Form has real-time feedback

#### ✓ Requirement 2: Message Channel Configuration
- RabbitMQ queue: `student.registration.queue`
- Exchange configured and bound
- Spring Integration channels set up

#### ✓ Requirement 3: Backend Listener
- Service activator listens to queue
- Receives messages from message channel
- Processes registration data

#### ✓ Requirement 4: Database Storage
- Data stored in PostgreSQL
- Registration timestamps recorded
- Multiple registrations tested successfully

### Verification Steps

**Test 1: Single Registration**
```bash
# Submit via frontend or curl
# Check database:
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT * FROM student_registrations ORDER BY id DESC LIMIT 1;"
```

**Test 2: Multiple Registrations**
```bash
# Submit 5-10 registrations
# Verify all stored:
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT COUNT(*) FROM student_registrations;"
```

**Test 3: Data Integrity**
```bash
# Verify all fields present:
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT student_name, student_id, email, program, year_level, registration_timestamp FROM student_registrations;"
```

## Conclusion

This system demonstrates a production-ready implementation of Enterprise Integration Patterns for a university registration system. The asynchronous, message-driven architecture allows for:

- **Scalability**: Handle high volumes of concurrent registrations
- **Reliability**: Message persistence ensures no data loss
- **Flexibility**: Easy to add new backend systems
- **Maintainability**: Clear separation of concerns

Students can extend this lab by:
- Adding more backend systems (Finance, Housing, Library)
- Implementing message transformation and enrichment
- Adding error handling and retry logic
- Implementing workflow orchestration
- Adding monitoring and alerting
