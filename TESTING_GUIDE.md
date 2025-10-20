# Testing Guide - RSU Registration System

## Unit Testing Scenarios

### Test 1: Basic Registration Submission

**Test Case**: Valid registration submission

**Steps**:
1. Open http://localhost:3000
2. Fill form with valid data:
   - Name: "John Doe"
   - Student ID: "RSU001001"
   - Email: "john.doe@rsu.edu"
   - Program: "Computer Science"
   - Year Level: "First Year"
3. Click "Submit Registration"

**Expected Results**:
- ✓ Form clears after submission
- ✓ Success message appears
- ✓ Frontend shows "SUBMITTED" status
- ✓ Network request shows 202 Accepted
- ✓ Data appears in database within 1-2 seconds

**Verification Commands**:
```bash
# Check database
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT * FROM student_registrations WHERE student_id='RSU001001';"

# Check backend logs for processing
tail -f logs/spring.log | grep "RSU001001"

# Check RabbitMQ queue (should be empty after processing)
# Visit http://localhost:15672 and check queue length
```

---

### Test 2: Form Validation

#### Test 2a: Missing Email

**Steps**:
1. Fill form but leave Email empty
2. Click Submit

**Expected Results**:
- ✓ Error message: "Email is required"
- ✓ Form stays filled with other data
- ✓ No request sent to backend

---

#### Test 2b: Invalid Email Format

**Steps**:
1. Enter email: "notanemail"
2. Click Submit

**Expected Results**:
- ✓ Error message: "Valid email is required"
- ✓ Request not sent to backend

---

#### Test 2c: Missing Student ID

**Steps**:
1. Fill all fields except Student ID
2. Click Submit

**Expected Results**:
- ✓ Error message: "Student ID is required"
- ✓ No request sent

---

### Test 3: Multiple Concurrent Registrations

**Steps**:
1. Open 3 browser tabs/windows
2. In each tab, submit different registrations
3. Submit simultaneously or near-simultaneously

**Expected Results**:
- ✓ All registrations succeed
- ✓ All data stored in database
- ✓ No data loss or corruption
- ✓ Each has unique ID

**Verification**:
```bash
# Count registrations
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT COUNT(*) FROM student_registrations;"

# List all registrations
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT student_id, student_name FROM student_registrations ORDER BY id DESC LIMIT 3;"
```

---

### Test 4: Message Queue Persistence

**Steps**:
1. Submit a registration
2. Stop the backend service (`Ctrl+C`)
3. Verify message is still in queue
4. Restart backend service

**Expected Results**:
- ✓ Message remains in RabbitMQ queue
- ✓ Backend processes message on restart
- ✓ Data is saved to database
- ✓ No message loss

**Verification**:
```bash
# Check queue size on RabbitMQ Management UI
# http://localhost:15672
# Queues tab -> student.registration.queue
```

---

### Test 5: Database Persistence

**Steps**:
1. Submit 5 registrations
2. Stop Docker containers: `docker-compose down`
3. Start containers again: `docker-compose up -d`
4. Query database

**Expected Results**:
- ✓ All 5 registrations still exist
- ✓ No data loss
- ✓ All timestamps preserved

**Verification**:
```bash
# After restart
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT COUNT(*) FROM student_registrations;"
```

---

### Test 6: Error Handling - Invalid Database

**Steps**:
1. Stop PostgreSQL: `docker-compose stop postgres`
2. Try to submit registration
3. Observe error handling
4. Start PostgreSQL: `docker-compose start postgres`

**Expected Results**:
- ✓ Frontend shows error message
- ✓ Message remains in queue
- ✓ No application crash
- ✓ Auto-recovery works when DB comes back

---

### Test 7: CORS Testing

**Steps**:
1. Open browser DevTools (F12)
2. Go to Console tab
3. Make request from different origin:
```javascript
fetch('http://localhost:8080/api/v1/registrations/submit', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    studentName: 'Test',
    studentId: 'TEST001',
    email: 'test@rsu.edu',
    program: 'Engineering',
    yearLevel: 'First Year'
  })
})
```

**Expected Results**:
- ✓ Request succeeds (CORS enabled)
- ✓ No CORS error in console

---

## Integration Testing

### Test Suite A: End-to-End Flow

**Objective**: Verify complete registration flow

**Preconditions**:
- All services running
- Database clean (or known state)

**Test Steps**:
```bash
# 1. Clear test data (optional)
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "DELETE FROM student_registrations WHERE student_id LIKE 'TEST%';"

# 2. Get initial count
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT COUNT(*) as initial_count FROM student_registrations;"

# 3. Submit registration
curl -X POST http://localhost:8080/api/v1/registrations/submit \
  -H "Content-Type: application/json" \
  -d '{
    "studentName": "Integration Test Student",
    "studentId": "TEST001",
    "email": "integration@rsu.edu",
    "program": "Computer Science",
    "yearLevel": "First Year"
  }'

# 4. Wait for processing (2 seconds)
sleep 2

# 5. Verify database
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT * FROM student_registrations WHERE student_id='TEST001';"

# 6. Verify get endpoint
curl http://localhost:8080/api/v1/registrations/student/TEST001

# 7. Check count
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT COUNT(*) as final_count FROM student_registrations;"
```

**Expected Results**:
- ✓ Registration submitted (202 Accepted)
- ✓ Message in queue temporarily visible
- ✓ Data appears in database
- ✓ GET endpoints return correct data
- ✓ Count increased by 1

---

### Test Suite B: RabbitMQ Message Flow

**Objective**: Verify message queue operation

**Steps**:
```bash
# 1. Check queue status on RabbitMQ UI
# http://localhost:15672
# Queues -> student.registration.queue
# Note: Messages ready count

# 2. Submit multiple registrations
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/v1/registrations/submit \
    -H "Content-Type: application/json" \
    -d "{
      \"studentName\": \"Batch Student $i\",
      \"studentId\": \"BATCH$i\",
      \"email\": \"batch$i@rsu.edu\",
      \"program\": \"Engineering\",
      \"yearLevel\": \"Second Year\"
    }" &
done
wait

# 3. Observe queue processing (watch RabbitMQ UI)
# Messages ready should increase then decrease

# 4. Verify all stored
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration \
  -c "SELECT COUNT(*) FROM student_registrations WHERE student_id LIKE 'BATCH%';"
```

**Expected Results**:
- ✓ Queue depth visible in RabbitMQ UI
- ✓ Messages processed within seconds
- ✓ Queue returns to 0
- ✓ 5 new records in database

---

### Test Suite C: Load Testing

**Objective**: Test system under load

**Tool**: Apache Bench or similar

```bash
# Create test data file
cat > /tmp/registration_test.json << 'EOF'
{
  "studentName": "Load Test Student",
  "studentId": "LOAD_$(date +%s%N)",
  "email": "loadtest@rsu.edu",
  "program": "Nursing",
  "yearLevel": "Third Year"
}
EOF

# Run load test (100 requests, 10 concurrent)
ab -n 100 -c 10 \
   -H "Content-Type: application/json" \
   -p /tmp/registration_test.json \
   http://localhost:8080/api/v1/registrations/submit
```

**Expected Results**:
- ✓ System processes all requests
- ✓ No timeout errors
- ✓ Response time under 500ms
- ✓ All data persisted
- ✓ No data corruption

---

## Performance Testing

### Response Time Testing

```bash
# Single registration response time
time curl -X POST http://localhost:8080/api/v1/registrations/submit \
  -H "Content-Type: application/json" \
  -d '{
    "studentName": "Performance Test",
    "studentId": "PERF001",
    "email": "perf@rsu.edu",
    "program": "Liberal Arts",
    "yearLevel": "Fourth Year"
  }'
```

**Expected Results**:
- API response time: < 100ms
- Total processing: < 500ms
- Database insert: < 50ms

---

### Database Query Performance

```bash
# Check slow queries
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration << EOF
SELECT * FROM student_registrations 
ORDER BY registration_timestamp DESC 
LIMIT 10;

-- Check index usage
EXPLAIN ANALYZE
SELECT * FROM student_registrations 
WHERE student_id = 'RSU001001';
EOF
```

---

## Checkpoint Verification Checklist

Use this checklist to verify all requirements are met:

### ✓ Requirement 1: Web Registration Form
- [ ] Form accepts all required fields (Name, ID, Email, Program, Year Level)
- [ ] Form validates required fields
- [ ] Form validates email format
- [ ] Form provides real-time feedback
- [ ] Form clears after successful submission
- [ ] Form disables submit button during processing
- [ ] Form displays success message
- [ ] Form displays error message on failure

### ✓ Requirement 2: Message Channel Configuration
- [ ] RabbitMQ queue created: `student.registration.queue`
- [ ] RabbitMQ exchange created: `student.registration.exchange`
- [ ] Queue and exchange are bound with correct routing key
- [ ] Spring Integration channels configured
- [ ] AMQP inbound adapter listening to queue
- [ ] Message conversion from JSON to DTO works

### ✓ Requirement 3: Backend Listener
- [ ] Service activator receives messages
- [ ] Messages are deserialized correctly
- [ ] Service layer is called with correct data
- [ ] Error handling for invalid messages
- [ ] Logging shows message processing
- [ ] Dead letter queue configured (optional)

### ✓ Requirement 4: Database Storage
- [ ] PostgreSQL database running
- [ ] `student_registrations` table exists
- [ ] All fields present: name, id, email, program, year_level, timestamp, status
- [ ] Student ID is unique
- [ ] Registration timestamp recorded automatically
- [ ] Status field tracks registration state
- [ ] Data survives container restart

### ✓ Data Loss Testing
- [ ] Submit registration while backend is stopped
- [ ] Message persists in queue
- [ ] Backend processes message on restart
- [ ] No data lost

### ✓ Concurrent Operations
- [ ] 5+ simultaneous registrations process without error
- [ ] All data correctly stored
- [ ] No duplicate IDs
- [ ] Timestamps differ appropriately

### ✓ API Endpoints
- [ ] POST /submit responds with 202 Accepted
- [ ] GET /{id} returns registration
- [ ] GET /student/{studentId} returns registration
- [ ] GET /stats/count returns correct count

---

## Test Data Sets

### Dataset 1: Valid Registrations
```json
[
  {
    "studentName": "Alice Johnson",
    "studentId": "RSU2024001",
    "email": "alice.johnson@rsu.edu",
    "program": "Computer Science",
    "yearLevel": "First Year"
  },
  {
    "studentName": "Bob Smith",
    "studentId": "RSU2024002",
    "email": "bob.smith@rsu.edu",
    "program": "Engineering",
    "yearLevel": "Second Year"
  },
  {
    "studentName": "Carol White",
    "studentId": "RSU2024003",
    "email": "carol.white@rsu.edu",
    "program": "Business Administration",
    "yearLevel": "Third Year"
  }
]
```

### Dataset 2: Invalid Registrations (for validation testing)
```json
[
  {
    "studentName": "Missing Email",
    "studentId": "RSU2024004",
    "program": "Nursing",
    "yearLevel": "First Year"
  },
  {
    "studentName": "Invalid Email",
    "studentId": "RSU2024005",
    "email": "not-an-email",
    "program": "Education",
    "yearLevel": "Second Year"
  },
  {
    "studentId": "RSU2024006",
    "email": "missing.name@rsu.edu",
    "program": "Liberal Arts",
    "yearLevel": "Third Year"
  }
]
```

---

## Logging for Debugging

### Enable Debug Logging
Edit `application.properties`:
```properties
logging.level.com.rsu.registration=DEBUG
logging.level.org.springframework.integration=DEBUG
logging.level.org.springframework.amqp=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Monitor Logs in Real-time
```bash
# Backend logs
mvn spring-boot:run | grep -E "(DEBUG|ERROR|INFO)"

# RabbitMQ logs
docker-compose logs -f rabbitmq | grep -E "(accepting|ERROR)"

# PostgreSQL logs
docker-compose logs -f postgres
```

---

## Success Criteria

The system is working correctly when:

1. **Frontend**: Form accepts input, validates, and sends to backend
2. **Message Queue**: Messages appear in queue and are consumed
3. **Integration Layer**: Messages processed without errors
4. **Backend Service**: Data retrieved from messages correctly
5. **Database**: Data persisted with all fields
6. **No Data Loss**: Message and data persistence verified
7. **Multiple Submissions**: Concurrent requests handled correctly
8. **System Resilience**: Recovery after component failures

**All 8 criteria must be met for successful lab completion.**
