# 🎯 QUICK START: View Your Message Queues NOW!

## ✅ Your RabbitMQ is Running!

### 🌐 EASIEST WAY - Open the Web Interface:

**Just click this URL:** 👇

## 🔗 http://localhost:15672

**Login with:**
- Username: `guest`
- Password: `guest`

---

## 📊 What You'll See:

### Current Queue Status:
```
Queue Name: student.registration.queue
Messages: 0 (processed quickly!)
Consumers: 1 (your backend is listening)
```

**✅ This is GOOD!** Zero messages means your consumers are processing them immediately.

---

## 🖱️ How to Use the RabbitMQ Dashboard:

### 1️⃣ Click "Queues" tab at the top
You'll see:
- `student.registration.queue` - Your registration messages

### 2️⃣ Click on the queue name to see details:
- **Overview**: Message rates, consumers
- **Get messages**: View actual message content
- **Bindings**: How messages are routed

### 3️⃣ To see a message:
1. Scroll down to **"Get Messages"** section
2. Set "Messages" to `1`
3. Click **"Get Message(s)"** button
4. You'll see JSON like:
   ```json
   {
     "studentId": "333333",
     "fullName": "Jaycee Aguilan",
     "email": "jaycee@rsu.edu",
     "program": "Computer Science",
     "yearLevel": "First Year"
   }
   ```

---

## 🧪 Test It Right Now:

### Step 1: Open RabbitMQ Dashboard
```
http://localhost:15672
```

### Step 2: Keep it open, go to "Queues" tab

### Step 3: Submit a new student registration
```
http://localhost:3000
```

### Step 4: Watch the queue!
You might see the message count briefly go to 1, then back to 0 as it's processed!

---

## 📈 Understanding the Numbers:

| What You See | What It Means |
|--------------|---------------|
| **Messages: 0** | ✅ All messages processed (good!) |
| **Messages: 5** | 🟡 5 messages waiting (might be temporary) |
| **Consumers: 1** | ✅ Your backend is listening |
| **Consumers: 0** | ❌ Backend not running! |

---

## 🔍 Command Line Alternative:

If you prefer terminal:

```powershell
# View all queues
docker exec rsu_rabbitmq rabbitmqctl list_queues name messages consumers

# View with details
docker exec rsu_rabbitmq rabbitmqctl list_queues name messages consumers message_stats

# View exchanges
docker exec rsu_rabbitmq rabbitmqctl list_exchanges
```

---

## 🎭 Your Message Flow Visualized:

```
📝 Student Registration Form (Frontend)
        ↓
    HTTP POST to Backend
        ↓
🐰 RabbitMQ Queue: student.registration.queue
        ↓
👂 Backend Consumer (listening)
        ↓
📊 Content-Based Router
        ↓
    ┌──────────┬──────────┐
    ↓          ↓          ↓
🏠 Housing  💰 Billing  📚 Library
    ↓          ↓          ↓
        Aggregator
           ↓
      💾 Database
```

---

## 🎯 Quick Commands:

### See your current queue status:
```powershell
docker exec rsu_rabbitmq rabbitmqctl list_queues
```

### See if backend is consuming:
```powershell
docker exec rsu_rabbitmq rabbitmqctl list_consumers
```

### See message rates:
Open: http://localhost:15672 → Overview tab → See graphs!

---

## 🚨 Troubleshooting:

### "Can't access localhost:15672"
Check if port is exposed:
```powershell
docker ps | Select-String "15672"
```
Should see: `0.0.0.0:15672->15672/tcp`

### "No queues shown"
Your queues are created when:
1. ✅ Backend starts (declares queues)
2. ✅ First message is sent

### "Messages stuck in queue"
Check if backend is running:
```powershell
# In backend folder
mvn spring-boot:run
```

---

## 🎊 Your Current Status:

✅ **RabbitMQ**: Running and healthy
✅ **Management UI**: Available at http://localhost:15672  
✅ **Queue**: `student.registration.queue` exists
✅ **Consumer**: 1 backend consumer listening
✅ **Messages**: 0 (being processed immediately)

**Everything is working perfectly!** 🚀

---

## 📚 More Details:

See `HOW_TO_VIEW_RABBITMQ_QUEUES.md` for complete documentation!

---

## 🔗 Your System URLs:

- 🌐 **Frontend**: http://localhost:3000
- 🔧 **Backend API**: http://localhost:8080
- 🐰 **RabbitMQ UI**: http://localhost:15672
- 🐘 **Database**: localhost:5432 (see HOW_TO_VIEW_DATABASE.md)

**Go to http://localhost:15672 right now to see your queues!** 👆
