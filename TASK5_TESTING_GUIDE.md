# 🧪 Task 5 Error Handling Testing Guide

## How to Generate Failed Messages for Testing

This guide provides multiple methods to trigger failures and test the error handling system.

---

## 📋 Prerequisites

Before testing, ensure:
- ✅ Backend is running on `http://localhost:8080`
- ✅ Frontend is running on `http://localhost:3000`
- ✅ PostgreSQL is running (Docker: `rsu_postgres`)
- ✅ RabbitMQ is running (Docker: `rsu_rabbitmq`)

---

## 🔥 Method 1: Stop RabbitMQ (Recommended - Easiest)

**This simulates a message queue failure and triggers the retry mechanism.**

### Step 1: Stop RabbitMQ
```powershell
docker stop rsu_rabbitmq
```

### Step 2: Submit a Registration
1. Go to http://localhost:3000
2. Fill out the registration form:
   - Student Number: `2025-001`
   - First Name: `Test`
   - Last Name: `Student`
   - Email: `test@student.com`
   - Phone: `09123456789`
   - Date of Birth: Any date
   - Program: Select any program
   - Year Level: Select any year
3. Click **Submit Registration**

### Step 3: Observe the Error
- ❌ Registration will fail
- 🔄 Backend will catch the error and send it to the error channel
- 💾 Failed message will be saved to `failed_messages` table with status `PENDING_RETRY`

### Step 4: Check Admin Dashboard
1. Navigate to http://localhost:3000/admin
2. Click **"Failed Registrations"** tab
3. You should see your failed registration with:
   - Status: `PENDING_RETRY`
   - Retry Attempt: `0`
   - Error Category: `SYSTEM_ERROR`

### Step 5: Start RabbitMQ and Watch Auto-Retry
```powershell
docker start rsu_rabbitmq
```

**What happens:**
- ⏰ Retry scheduler runs every 5 seconds
- 🔄 System will automatically retry (3 attempts: 5s, 10s, 20s)
- ✅ If successful, status changes to `RETRY_SUCCESS`
- ❌ If all retries fail, moves to Dead Letter Queue (`FAILED`)

---

## 💥 Method 2: Stop PostgreSQL Database

**This simulates a database connection failure.**

### Step 1: Stop PostgreSQL
```powershell
docker stop rsu_postgres
```

### Step 2: Submit Registration
- Follow same steps as Method 1

### Step 3: Expected Result
- Backend will throw database connection error
- Error logged to console (database unavailable)
- Once database restarts, backend will need restart too

### Step 4: Restart Services
```powershell
# Start PostgreSQL
docker start rsu_postgres

# Restart backend (in backend directory)
cd rsu-registration-backend
mvn spring-boot:run
```

---

## 🌐 Method 3: Invalid Data Validation

**Test validation errors and data integrity.**

### Scenario A: Duplicate Student Number
1. Submit a registration with Student Number: `2025-001`
2. Submit ANOTHER registration with SAME Student Number: `2025-001`
3. Second submission should fail with validation error

### Scenario B: Invalid Email Format
1. Enter invalid email: `notanemail` (without @)
2. Frontend should show validation error
3. Backend will also validate and reject

### Scenario C: Missing Required Fields
1. Leave required fields empty
2. Try to submit
3. Should show validation errors

---

## ⏱️ Method 4: Simulate Network Timeout

**Modify the service to throw timeout exceptions.**

### Option A: Add Artificial Delay in Service

Edit `RegistrationService.java` temporarily:

```java
public Student saveStudent(Student student) {
    try {
        // Artificial delay to simulate timeout
        Thread.sleep(35000); // 35 seconds - exceeds typical timeout
        
        Student savedStudent = studentRepository.save(student);
        messagingService.sendRegistrationMessage(savedStudent);
        return savedStudent;
    } catch (Exception e) {
        // Will trigger error handling
        throw new RuntimeException("Timeout occurred", e);
    }
}
```

### Option B: Stop Backend Temporarily

1. Stop the backend server (Ctrl+C)
2. Try to submit registration from frontend
3. Frontend will get network error
4. Restart backend

---

## 🎯 Method 5: Manual Error Injection

**Create a test endpoint to directly inject errors.**

### Add Test Controller (Temporary)

Create `TestErrorController.java`:

```java
package com.example.registration.controller;

import com.example.registration.model.ErrorCategory;
import com.example.registration.service.ErrorChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:3000")
public class TestErrorController {
    
    @Autowired
    private ErrorChannelService errorChannelService;
    
    @PostMapping("/inject-error")
    public String injectError(@RequestParam String errorType) {
        String testPayload = "{\"studentNumber\":\"TEST-ERROR\",\"firstName\":\"Test\"}";
        
        ErrorCategory category = ErrorCategory.valueOf(errorType);
        Exception testException = new RuntimeException("Test error: " + errorType);
        
        errorChannelService.sendToErrorChannel(
            testPayload,
            testException,
            "TEST",
            category
        );
        
        return "Error injected successfully";
    }
}
```

### Use the Endpoint

```powershell
# Inject SYSTEM_ERROR
curl -X POST "http://localhost:8080/api/test/inject-error?errorType=SYSTEM_ERROR"

# Inject VALIDATION_ERROR
curl -X POST "http://localhost:8080/api/test/inject-error?errorType=VALIDATION_ERROR"

# Inject NETWORK_ERROR
curl -X POST "http://localhost:8080/api/test/inject-error?errorType=NETWORK_ERROR"
```

---

## 📊 Testing the Complete Flow

### Test Case 1: Automatic Retry Success

**Steps:**
1. Stop RabbitMQ: `docker stop rsu_rabbitmq`
2. Submit registration → Fails
3. Check Admin Dashboard → Status: `PENDING_RETRY`, Attempt: 0
4. Start RabbitMQ: `docker start rsu_rabbitmq`
5. Wait 5 seconds → First retry (Attempt: 1)
6. If successful → Status: `RETRY_SUCCESS` ✅

**Expected Timeline:**
- T=0s: Initial failure
- T=5s: Retry attempt 1
- T=15s: Retry attempt 2 (if #1 fails)
- T=35s: Retry attempt 3 (if #2 fails)
- T=36s: Move to DLQ if all fail

---

### Test Case 2: Dead Letter Queue (DLQ)

**Steps:**
1. Stop RabbitMQ: `docker stop rsu_rabbitmq`
2. Submit registration → Fails
3. **Keep RabbitMQ stopped**
4. Wait for all 3 retry attempts (~35 seconds)
5. Check Admin Dashboard:
   - Status: `FAILED` (in Dead Letter Queue)
   - Retry Attempt: `3`
   - All attempts exhausted

---

### Test Case 3: Manual Retry from DLQ

**Steps:**
1. Complete Test Case 2 to get a failed message in DLQ
2. Start RabbitMQ: `docker start rsu_rabbitmq`
3. Go to Admin Dashboard → **"Failed Registrations"** tab
4. Find the failed message (Status: `FAILED`)
5. Click **"Retry"** button
6. Watch status change:
   - `FAILED` → `MANUAL_RETRY_SUCCESS` ✅

---

## 🔍 What to Check in Admin Dashboard

### Error Logs Tab
- **Total errors** count
- **Error trends** by hour/day
- **Error categories** breakdown:
  - 🔴 SYSTEM_ERROR
  - 🟡 VALIDATION_ERROR
  - 🔵 NETWORK_ERROR
  - 🟣 DATABASE_ERROR
- **Recent errors** list with timestamps

### Failed Registrations Tab
- **All failed messages** in table
- **Status column** showing:
  - `PENDING_RETRY` - Waiting for retry
  - `RETRY_SUCCESS` - Auto-retry worked
  - `FAILED` - In Dead Letter Queue
  - `MANUAL_RETRY_SUCCESS` - Manual retry worked
- **Retry count** (0-3)
- **Next retry time** countdown
- **Retry button** for DLQ messages

### System Metrics Tab
- **Success rate** percentage
- **Total registrations** (success + failed)
- **Active retries** count
- **DLQ size** (messages in dead letter queue)

---

## 🎬 Quick Test Scenario (5 Minutes)

**Complete walkthrough to see everything working:**

```powershell
# 1. Stop RabbitMQ
docker stop rsu_rabbitmq

# 2. Open browser to http://localhost:3000

# 3. Submit a registration form (use any data)

# 4. See error message in frontend

# 5. Open Admin Dashboard: http://localhost:3000/admin

# 6. Check "Failed Registrations" tab - you'll see your failed message

# 7. Start RabbitMQ
docker start rsu_rabbitmq

# 8. Watch the retry happen (refresh dashboard after 5-10 seconds)

# 9. See status change from PENDING_RETRY → RETRY_SUCCESS

# 10. Check "Error Logs" tab to see error details
```

---

## 🐛 Debugging Tips

### Check Backend Logs
Look for these messages:
```
ERROR [ErrorChannelService] - Failed to process message
INFO  [RetryService] - Processing retry queue...
INFO  [RetryService] - Retrying message ID: xxx (Attempt 1/3)
INFO  [RetryService] - Message xxx successfully retried
ERROR [RetryService] - All retry attempts exhausted for message xxx
```

### Check Database Tables
```sql
-- Check failed messages
SELECT id, retry_status, retry_attempt, error_category, created_at 
FROM failed_messages 
ORDER BY created_at DESC;

-- Check error logs
SELECT id, error_message, error_category, error_timestamp 
FROM error_logs 
ORDER BY error_timestamp DESC;
```

### Check RabbitMQ Management UI
- Open: http://localhost:15672
- Login: guest / guest
- Check queues:
  - `registration.queue` - Main queue
  - Message counts and rates

---

## ✅ Success Indicators

You'll know the system is working when:

1. ✅ Failed registrations appear in Admin Dashboard
2. ✅ Retry status updates automatically every 5 seconds
3. ✅ Retry count increments (0 → 1 → 2 → 3)
4. ✅ Status changes: `PENDING_RETRY` → `RETRY_SUCCESS` or `FAILED`
5. ✅ Manual retry button works for DLQ messages
6. ✅ Error logs show detailed error information
7. ✅ System metrics update in real-time

---

## 📝 Test Results Checklist

Use this checklist to verify all features:

- [ ] Automatic error capture when RabbitMQ is down
- [ ] Failed message saved to database with correct status
- [ ] Retry scheduler runs every 5 seconds
- [ ] Exponential backoff delays (5s, 10s, 20s)
- [ ] Status updates after each retry attempt
- [ ] Successful retry changes status to RETRY_SUCCESS
- [ ] Failed retries move to DLQ after 3 attempts
- [ ] Manual retry works from Admin Dashboard
- [ ] Error logs capture full error details
- [ ] Admin Dashboard shows real-time updates
- [ ] System metrics calculate correctly
- [ ] Frontend displays error notifications

---

## 🚀 Advanced Testing

### Load Testing
```powershell
# Stop RabbitMQ
docker stop rsu_rabbitmq

# Submit multiple registrations quickly (10-20)
# All should be captured and queued for retry

# Start RabbitMQ
docker start rsu_rabbitmq

# Watch all automatically retry in batches
```

### Concurrent Retry Testing
- Create multiple failures
- All should retry independently
- Check that retry scheduler handles concurrent retries

### Data Integrity Testing
- Verify no duplicate registrations after retries
- Check that failed messages don't create partial data
- Ensure error logs match failed messages

---

## 📞 Need Help?

If something doesn't work:

1. **Check backend logs** for exceptions
2. **Check Docker containers** are running: `docker ps`
3. **Check database** for failed_messages: `SELECT * FROM failed_messages;`
4. **Restart services** in order: PostgreSQL → RabbitMQ → Backend → Frontend
5. **Clear browser cache** and refresh

---

## 🎉 Happy Testing!

The error handling system is designed to be resilient and self-healing. Most failures will automatically recover once the underlying issue is resolved!
