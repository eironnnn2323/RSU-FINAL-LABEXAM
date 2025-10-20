# Quick Start Guide - RSU Registration System

## 5-Minute Setup

### Step 1: Start Infrastructure (2 min)

```bash
# Navigate to docker directory
cd docker

# Start RabbitMQ and PostgreSQL
docker-compose up -d

# Verify services are running
docker-compose ps
```

**Expected Output:**
```
CONTAINER ID   IMAGE                                   STATUS
xxx            rabbitmq:3.12-management-alpine        Up 2 seconds
yyy            postgres:15-alpine                      Up 2 seconds
```

### Step 2: Build and Run Backend (2 min)

**Terminal 1 - Backend:**
```bash
cd rsu-registration-backend
mvn spring-boot:run
```

**Wait for:**
```
Started RegistrationApplication in X seconds
```

### Step 3: Start Frontend (1 min)

**Terminal 2 - Frontend:**
```bash
cd rsu-registration-frontend
npm install  # First time only
npm start
```

**Browser opens automatically to:** http://localhost:3000

---

## Test It!

### Option A: Web Form (Recommended)
1. Go to http://localhost:3000
2. Fill out the form
3. Click "Submit Registration"
4. See success message

### Option B: cURL Command
```bash
curl -X POST http://localhost:8080/api/v1/registrations/submit \
  -H "Content-Type: application/json" \
  -d '{
    "studentName": "Test Student",
    "studentId": "RSU123456",
    "email": "test@rsu.edu",
    "program": "Computer Science",
    "yearLevel": "First Year"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Registration submitted successfully. Processing...",
  "status": "SUBMITTED"
}
```

### Option C: Postman
1. Import `RSU_Registration_API.postman_collection.json` into Postman
2. Run "Submit Registration - Valid" request
3. See results in Postman

---

## Verify Data in Database

```bash
# Connect to PostgreSQL container
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration

# In psql prompt, run:
SELECT * FROM student_registrations;
```

---

## Monitor System

### RabbitMQ Management UI
- URL: http://localhost:15672
- Username: guest
- Password: guest
- Navigate to "Queues" tab
- Look for `student.registration.queue`

### Backend Logs
- Check Terminal 1 for Spring Boot logs
- Search for "Processing registration"

### Frontend Console
- Open browser DevTools (F12)
- Check Console tab for API calls

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Connection refused" | Run `docker-compose up -d` |
| Port 8080 already in use | `npx kill-port 8080` |
| Port 3000 already in use | `npx kill-port 3000` |
| Database error | Check PostgreSQL: `docker-compose logs postgres` |
| RabbitMQ error | Check RabbitMQ: `docker-compose logs rabbitmq` |

---

## Next Steps

1. ✅ Submit multiple registrations
2. ✅ Check data in database
3. ✅ Monitor RabbitMQ queue
4. ✅ Review logs and understand the flow
5. 📖 Read full README.md for architecture details

---

## Key URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Frontend | http://localhost:3000 | N/A |
| Backend API | http://localhost:8080 | N/A |
| RabbitMQ Management | http://localhost:15672 | guest/guest |
| PostgreSQL | localhost:5432 | rsu_user/rsu_password |

---

## Stop Services

```bash
# Stop all services
docker-compose down

# Stop frontend (Ctrl+C in Terminal 2)
# Stop backend (Ctrl+C in Terminal 1)
```

---

## Clean Up

```bash
# Remove containers and volumes
docker-compose down -v

# Remove node_modules and npm cache
cd rsu-registration-frontend
rm -rf node_modules
npm cache clean --force
```

---

**Need help?** Check README.md for detailed documentation!
