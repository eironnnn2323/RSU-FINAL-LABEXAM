# 🧪 Quick Test Guide - Content-Based Routing

## Current Status
- ✅ Backend: Running on port 8080 with Content-Based Routing
- ✅ Frontend: http://localhost:3000
- ✅ Docker Services: PostgreSQL + RabbitMQ running

---

## 🎯 Quick Tests

### Test 1: First Year Student (Housing Route) 🏠

**Fill the form with:**
```
Student Name: John Doe
Student ID: 2024-00001
Email: john.doe@rsu.edu
Program: Computer Science
Year Level: First Year  ← This is key!
```

**Click "Submit Registration"**

**Expected Result:**
- ✅ Green success message
- ✅ Badge: "🎓 First Year Student" (purple)
- ✅ Routing message: "First-year student → Routed to Housing..."
- ✅ Systems list shows:
  - 🏠 Housing System
  - 📚 Library System

**Backend logs should show:**
```
🔀 ROUTER: Routing John Doe to HOUSING SYSTEM (First Year Student)
🏠 HOUSING SYSTEM: Processing housing allocation...
🏠 HOUSING SYSTEM: John Doe assigned to Tech Hall
📚 LIBRARY SYSTEM: Activating library account...
```

---

### Test 2: Returning Student (Billing Route) 💰

**Fill the form with:**
```
Student Name: Jane Smith
Student ID: 2023-00123
Email: jane.smith@rsu.edu
Program: Business Administration
Year Level: Second Year  ← This is key!
```

**Click "Submit Registration"**

**Expected Result:**
- ✅ Green success message
- ✅ Badge: "📖 Returning Student" (pink)
- ✅ Routing message: "Returning student → Routed to Billing..."
- ✅ Systems list shows:
  - 💰 Billing System
  - 📚 Library System

**Backend logs should show:**
```
🔀 ROUTER: Routing Jane Smith to BILLING SYSTEM (Returning Student)
💰 BILLING SYSTEM: Processing fee calculation...
💰 BILLING SYSTEM: Jane Smith - Total fees: ₱36,500.00
📚 LIBRARY SYSTEM: Activating library account...
```

---

### Test 3: Different Year Levels

Try submitting with each year level to see different routing:

| Year Level    | Routes To             | Badge Color | Fee Multiplier |
|---------------|----------------------|-------------|----------------|
| First Year    | Housing + Library    | Purple 🎓   | N/A            |
| Second Year   | Billing + Library    | Pink 📖     | 1.05x          |
| Third Year    | Billing + Library    | Pink 📖     | 1.10x          |
| Fourth Year   | Billing + Library    | Pink 📖     | 1.15x          |

---

### Test 4: Different Programs (First Year)

Test first-year students with different programs to see dorm assignments:

| Program                  | Dormitory Building      |
|-------------------------|-------------------------|
| Computer Science        | Tech Hall               |
| Engineering             | Tech Hall               |
| Business Administration | Commerce Building       |
| Liberal Arts            | Liberal Arts Residence  |
| Nursing                 | Main Campus Dormitory   |
| Education               | Main Campus Dormitory   |

---

## 🔍 Where to Look

### Frontend (http://localhost:3000)
- Success message with routing info
- Color-coded badge (purple for first-year, pink for returning)
- List of systems processing the registration
- Info box explaining the routing rules

### Backend Logs (PowerShell Window)
Look for these emoji indicators:
- 📨 = Received registration
- 🔀 = Router making decision
- 🏠 = Housing System processing
- 💰 = Billing System processing
- 📚 = Library System processing
- ✅ = Successfully completed
- 📊 = Routing summary

### Database (Optional)
```sql
-- Connect to PostgreSQL
psql -h localhost -U rsu_user -d rsu_registration

-- View registrations with routing info
SELECT 
    id, 
    student_name, 
    year_level, 
    status,
    LEFT(message, 100) as routing_summary
FROM student_registrations
ORDER BY id DESC
LIMIT 5;
```

---

## 🐛 Troubleshooting

### Frontend not showing routing info
- Hard refresh: Ctrl+F5
- Clear browser cache
- Check browser console for errors

### Backend not routing
- Check PowerShell window for errors
- Verify backend is running: `curl http://localhost:8080/api/v1/registrations/health`
- Restart backend if needed

### RabbitMQ connection issues
- Check Docker: `docker ps`
- Restart containers: `cd docker; docker compose restart`

---

## ✅ Success Criteria

You've successfully tested Content-Based Routing when:

1. ✅ First-year students show Housing route
2. ✅ Returning students show Billing route
3. ✅ All students show Library route
4. ✅ Backend logs show routing decisions with emojis
5. ✅ Frontend displays routing information in real-time
6. ✅ Different programs route to different dorms
7. ✅ Database stores routing details

---

## 🎉 All Tests Passing?

**Congratulations!** Your Content-Based Routing implementation is working perfectly!

The system now intelligently routes student registrations based on their year level:
- 🏠 First-year students get dormitory allocation
- 💰 Returning students get fee calculation
- 📚 Everyone gets library access

**You've successfully implemented the Content-Based Router EIP pattern!** 🚀
