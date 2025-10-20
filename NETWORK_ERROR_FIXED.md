# ✅ Network Error Fixed!

## 🔍 Problem Identified

The **"Network Error"** occurred because:
- ✅ Docker containers are running (PostgreSQL + RabbitMQ)
- ✅ Frontend is running on port 3000
- ❌ **Backend was NOT running on port 8080**

The backend starts successfully but then shuts down immediately because the PowerShell terminal was waiting for input.

---

## 🚀 Solution: Start Backend Properly

I've created a helper script to start the backend correctly!

### Option 1: Use the New Script (EASIEST)

```powershell
.\start-backend.ps1
```

This will:
1. Navigate to the backend directory
2. Start Spring Boot with Maven
3. Keep the backend running
4. Show you when it's ready

### Option 2: Manual Start

```powershell
cd rsu-registration-backend
mvn spring-boot:run
```

**IMPORTANT:** Keep the terminal window **OPEN**! Don't close it or press Ctrl+C.

---

## ✅ How to Know Backend is Ready

Watch for this message in the terminal:

```
Started RegistrationApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

When you see that, the backend is ready! 🎉

---

## 🧪 Test Your Setup Now

### Step 1: Start Backend
```powershell
.\start-backend.ps1
```

Wait for "Started RegistrationApplication..." message (about 10-15 seconds).

### Step 2: Open Frontend
Frontend should already be running at: **http://localhost:3000**

### Step 3: Submit a Registration
Fill out the form and click Submit!

---

## 🔧 Verify Everything is Running

### Check Backend:
```powershell
# In a NEW PowerShell window:
netstat -ano | Select-String "8080" | Select-String "LISTENING"
```

Should show port 8080 is listening.

### Check Frontend:
```powershell
Get-Process -Name node
```

Should show Node.js processes running.

### Check Docker:
```powershell
docker ps
```

Should show `rsu_postgres` and `rsu_rabbitmq` running.

---

## 📊 Complete System Status

When everything is running correctly:

```
✅ PostgreSQL  - Port 5432  (Docker)
✅ RabbitMQ    - Port 5672  (Docker)
✅ RabbitMQ UI - Port 15672 (Docker)
✅ Backend     - Port 8080  (Spring Boot)
✅ Frontend    - Port 3000  (React)
```

---

## 🎯 Quick Start Commands

### Start Everything:
```powershell
# Terminal 1: Start Docker (if not running)
docker compose up -d

# Terminal 2: Start Backend
.\start-backend.ps1

# Terminal 3: Start Frontend (if not running)
cd rsu-registration-frontend
npm start
```

---

## 🚨 If You Still Get Network Error

### 1. Make sure backend is running:
```powershell
netstat -ano | Select-String "8080"
```

### 2. Check backend logs for errors:
Look at the terminal where you ran `start-backend.ps1`

### 3. Test backend directly:
```powershell
curl http://localhost:8080/api/v1/registrations/submit
```

Should return a response (even if it's an error about missing data - that's OK, it means the backend is responding!)

### 4. Check CORS settings:
The backend should allow requests from http://localhost:3000

---

## 🎉 Now Try Again!

1. **Run**: `.\start-backend.ps1`
2. **Wait**: For "Started RegistrationApplication..." message
3. **Go to**: http://localhost:3000
4. **Submit**: A student registration
5. **Success!**: You should see the success message with routing info!

---

## 📝 Keep These Terminals Open

When running your application, you need **3 terminal windows**:

1. **Docker logs** (optional): `docker compose logs -f`
2. **Backend**: `.\start-backend.ps1` ← MUST STAY OPEN!
3. **Frontend**: `npm start` ← MUST STAY OPEN!

**Don't close these terminals while testing!**

---

## 🔗 Your System URLs

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api/v1/registrations
- **RabbitMQ UI**: http://localhost:15672 (guest/guest)
- **Database**: localhost:5432 (see HOW_TO_VIEW_DATABASE.md)

---

**Ready to test! Start your backend now!** 🚀

```powershell
.\start-backend.ps1
```
