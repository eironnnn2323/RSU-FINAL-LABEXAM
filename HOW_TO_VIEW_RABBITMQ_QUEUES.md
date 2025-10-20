# 🐰 How to View RabbitMQ Message Queues

## 🎯 What You're Looking For

When students register, your system sends messages through **RabbitMQ queues**:
- 📤 **Registration Queue**: Initial registration messages
- 🏠 **Housing Queue**: Housing system messages
- 💰 **Billing Queue**: Billing system messages  
- 📚 **Library Queue**: Library system messages

---

## 🌐 Option 1: RabbitMQ Management UI (EASIEST!)

RabbitMQ has a built-in web interface!

### Access the Dashboard:
1. Open your browser and go to: **http://localhost:15672**
2. Login with:
   - **Username**: `guest`
   - **Password**: `guest`

### What You'll See:
- **Overview**: Total messages, message rates
- **Queues tab**: All your queues and message counts
- **Exchanges tab**: Where messages are routed from

---

## 📊 View Your Queues in the Web UI

### Step-by-Step:

1. **Go to http://localhost:15672** and login (guest/guest)

2. **Click the "Queues" tab** at the top

3. **You'll see your queues:**
   ```
   Queue Name                    | Messages Ready | Message Rate
   ------------------------------|----------------|-------------
   registration.queue            | 0              | 0/s
   housing.queue                 | 0              | 0/s
   billing.queue                 | 0              | 0/s
   library.queue                 | 0              | 0/s
   ```

4. **Click on any queue name** to see:
   - Message details
   - Message rates (in/out)
   - Consumers connected
   - Get messages (read messages from queue)

---

## 📨 View Actual Messages

### In the RabbitMQ Web UI:

1. Click on a queue name (e.g., `registration.queue`)
2. Scroll down to **"Get Messages"** section
3. Set **"Messages"** to `10` (how many to retrieve)
4. Click **"Get Message(s)"** button
5. You'll see the message payload (JSON format)

**Example Message:**
```json
{
  "studentId": "333333",
  "fullName": "Jaycee Aguilan",
  "email": "jaycee@rsu.edu",
  "program": "Computer Science",
  "yearLevel": "First Year"
}
```

**⚠️ Note:** Getting messages will **remove them from the queue** (they're acknowledged)!

---

## 🖥️ Option 2: Command Line (Docker)

### Check if RabbitMQ is Running:
```powershell
docker ps | Select-String "rabbitmq"
```

Expected output:
```
rsu_rabbitmq   rabbitmq:3-management   Up 2 hours   0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
```

### List All Queues:
```powershell
docker exec rsu_rabbitmq rabbitmqctl list_queues
```

Expected output:
```
Timeout: 60.0 seconds ...
Listing queues for vhost / ...
name                    messages
registration.queue      0
housing.queue           0
billing.queue           0
library.queue           0
```

### See Queue Details:
```powershell
docker exec rsu_rabbitmq rabbitmqctl list_queues name messages consumers message_stats
```

### See Exchanges:
```powershell
docker exec rsu_rabbitmq rabbitmqctl list_exchanges
```

Expected output:
```
Listing exchanges for vhost / ...
name                    type
                        direct
amq.direct             direct
amq.fanout             fanout
amq.headers            headers
amq.match              headers
amq.rabbitmq.trace     topic
amq.topic              topic
registration.exchange   direct
```

---

## 🔍 Option 3: Check Your Backend Logs

Your Spring Boot application logs when it sends/receives messages!

### View Backend Logs:
```powershell
cd backend
mvn spring-boot:run
```

Look for log entries like:
```
✅ Sending registration message to queue
📨 Received message from registration.queue
🏠 Routing to Housing System
📚 Routing to Library System
✅ Aggregation complete for student 333333
```

---

## 🎯 Understanding Your Message Flow

### 1️⃣ Registration Submitted (Frontend → Backend)
```
Frontend (localhost:3000)
    ↓ HTTP POST
Backend REST API (/api/students/register)
    ↓ Publishes message
RabbitMQ (registration.exchange → registration.queue)
```

### 2️⃣ Content-Based Router Processes
```
Registration Consumer
    ↓ Reads from registration.queue
Content-Based Router
    ↓ Routes based on yearLevel
Housing/Billing Queue + Library Queue
```

### 3️⃣ Aggregator Collects Responses
```
Housing Consumer & Library Consumer
    ↓ Process messages
Aggregator Service
    ↓ Combines responses from all systems
Database (PostgreSQL)
```

---

## 📊 Quick Reference: Your Queue Names

Based on your Spring Boot configuration:

| Queue Name | Purpose | Who Sends | Who Receives |
|------------|---------|-----------|--------------|
| `registration.queue` | Initial registrations | REST API | Content-Based Router |
| `housing.queue` | First year housing | Router | Housing Consumer |
| `billing.queue` | Returning student billing | Router | Billing Consumer |
| `library.queue` | Library access | Router | Library Consumer |

---

## 🚀 Test Message Flow

### Send a Test Registration:
1. Go to **http://localhost:3000**
2. Fill out the registration form
3. Submit

### Watch the Queues:
1. **Immediately open**: http://localhost:15672
2. Go to **Queues** tab
3. You should see message counts increase briefly then go back to 0
   - Why? Because consumers process messages quickly!

### See Processed Messages:
Check your database:
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT student_id, student_name, status FROM student_registrations ORDER BY registration_timestamp DESC LIMIT 5;"
```

---

## 🔧 Troubleshooting

### Can't Access RabbitMQ UI?

**Check if port 15672 is exposed:**
```powershell
docker ps | Select-String "15672"
```

Should see: `0.0.0.0:15672->15672/tcp`

**If not exposed, check your docker-compose.yml:**
```yaml
rabbitmq:
  ports:
    - "5672:5672"    # AMQP port
    - "15672:15672"  # Management UI port ← Must be here!
```

### Queues Show 0 Messages?

This is **NORMAL**! It means:
- ✅ Consumers are processing messages quickly
- ✅ Your system is working efficiently
- ✅ Messages are being consumed faster than they accumulate

### Want to See Messages Before They're Consumed?

**Option 1**: Temporarily stop consumers:
```powershell
# Stop backend (stops all consumers)
# Then submit a registration
# Check queues (messages will accumulate)
# Restart backend to process them
```

**Option 2**: Use the "Get Messages" feature in RabbitMQ UI (non-destructive peek)

---

## 📈 Monitor Message Rates

In RabbitMQ Management UI (http://localhost:15672):

1. Click **Overview** tab
2. See graphs:
   - **Message rates**: How many messages/second
   - **Queued messages**: Total in all queues
   - **Publish/Deliver rates**: Throughput

3. Click **Queues** tab
4. See per-queue rates:
   - **Incoming**: Messages being published
   - **Deliver/Ack**: Messages being consumed

---

## 🎉 Summary

**To view message queues:**
1. **Web UI** (easiest): http://localhost:15672 (guest/guest)
2. **Command line**: `docker exec rsu_rabbitmq rabbitmqctl list_queues`
3. **Backend logs**: Watch Spring Boot console output

**Why queues might be empty:**
- ✅ Consumers are fast (good thing!)
- ✅ Messages are processed immediately
- ✅ Check database to confirm messages were processed

**Your message flow is working when:**
- ✅ Queues show 0 or low message counts (being consumed)
- ✅ Database shows new registrations
- ✅ Status is PROFILE_COMPLETE
- ✅ No errors in backend logs

---

## 🔗 Quick Links

- **RabbitMQ Management UI**: http://localhost:15672
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Database queries**: See `HOW_TO_VIEW_DATABASE.md`

**Your messaging system is working!** 🚀
