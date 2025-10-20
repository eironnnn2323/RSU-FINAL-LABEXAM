# 🚀 How to Run RSU Student Registration System

## Current Status ✅
- ✅ **PostgreSQL** - Running on port 5432
- ✅ **RabbitMQ** - Running on port 5672  
- ✅ **Backend API** - Running on port 8080
- ❌ **Frontend** - Not running (needs to be started)

---

## Quick Start (Frontend Only)

Since your backend is already running, you just need to start the frontend:

### Option 1: Simple Command (Recommended)
```powershell
cd rsu-registration-frontend
npm start
```

Wait for "Compiled successfully!" message, then open: **http://localhost:3000**

---

## Full System Restart (If Needed)

### Step 1: Start Docker Services
```powershell
cd docker
docker compose up -d
```
Wait for containers to be healthy (~10 seconds)

### Step 2: Start Backend
```powershell
cd ..\rsu-registration-backend
java -jar target\rsu-registration-backend-1.0.0.jar
```
Wait for "Started RegistrationApplication" message (~7 seconds)

### Step 3: Start Frontend (New Terminal)
```powershell
cd ..\rsu-registration-frontend
npm start
```
Browser should open automatically to http://localhost:3000

---

## 🎯 Using the Application

### 1. Open Browser
Navigate to: **http://localhost:3000**

### 2. Fill Registration Form
- **Student Name**: John Doe
- **Student ID**: 2024-00001
- **Email**: john.doe@rsu.edu
- **Program**: Computer Science
- **Year Level**: 3

### 3. Submit
Click "Register Student" button

### 4. Success!
You should see: ✅ **"Student registered successfully!"**

---

## 🔍 Verify Services

### Check Backend Health
```powershell
curl http://localhost:8080/api/v1/registrations/health
```
Should return: **"Registration service is running"**

### Check Docker Services
```powershell
docker ps
```
Should show:
- `rsu_postgres` - healthy
- `rsu_rabbitmq` - healthy

### Check RabbitMQ Management
Open: http://localhost:15672
- Username: `guest`
- Password: `guest`

You can see messages flowing through the queue in real-time!

---

## 🛑 Stop Services

### Stop Frontend
Press `Ctrl+C` in the frontend terminal

### Stop Backend  
Press `Ctrl+C` in the backend terminal

### Stop Docker Services
```powershell
cd docker
docker compose down
```

---

## 🐛 Troubleshooting

### Backend Won't Start
**Check if port 8080 is in use:**
```powershell
netstat -ano | findstr :8080
```

**Kill existing Java processes:**
```powershell
taskkill /F /IM java.exe
```

Then restart backend.

### Frontend Won't Start
**Check if port 3000 is in use:**
```powershell
netstat -ano | findstr :3000
```

**Kill existing Node processes:**
```powershell
taskkill /F /IM node.exe
```

Then restart frontend.

### Database Connection Issues
**Restart Docker containers:**
```powershell
cd docker
docker compose restart
```

Wait 10 seconds, then restart backend.

---

## 📦 Rebuild Backend (If Code Changed)

```powershell
cd rsu-registration-backend
mvn clean install -DskipTests
```

Then start with:
```powershell
java -jar target\rsu-registration-backend-1.0.0.jar
```

---

## 🎊 You're All Set!

Your system implements **7 Enterprise Integration Patterns**:
1. ✅ Message Channel
2. ✅ Message Endpoint  
3. ✅ Message Transformer
4. ✅ Service Activator
5. ✅ Publish-Subscribe Channel
6. ✅ Message Router
7. ✅ AMQP Integration

**Ready for your demo!** 🚀
