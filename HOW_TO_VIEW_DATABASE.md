# 📊 How to View Database Data

## Option 1: Using Docker Command Line (Easiest)

### View All Student Registrations:
```powershell
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT * FROM student_registrations;"
```

### View Specific Columns:
```powershell
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT id, student_id, full_name, program, year_level, status FROM student_registrations;"
```

### Count Total Registrations:
```powershell
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT COUNT(*) as total_students FROM student_registrations;"
```

### View Latest Registration:
```powershell
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT * FROM student_registrations ORDER BY created_at DESC LIMIT 1;"
```

### View All Tables in Database:
```powershell
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration -c "\dt"
```

---

## Option 2: Interactive PostgreSQL Shell

### Enter PostgreSQL Shell:
```powershell
docker exec -it rsu_postgres psql -U rsu_user -d rsu_registration
```

### Then you can run SQL commands:
```sql
-- View all data
SELECT * FROM student_registrations;

-- View formatted
\x
SELECT * FROM student_registrations;

-- View table structure
\d student_registrations

-- Exit when done
\q
```

---

## Option 3: Using a GUI Tool (pgAdmin, DBeaver, etc.)

### Connection Details:
```
Host:       localhost
Port:       5432
Database:   rsu_registration
Username:   rsu_user
Password:   rsu_password
```

### Popular Tools:
- **pgAdmin** - https://www.pgadmin.org/
- **DBeaver** - https://dbeaver.io/
- **DataGrip** - https://www.jetbrains.com/datagrip/
- **Azure Data Studio** - With PostgreSQL extension

---

## Option 4: Via Backend API

### Get All Registrations:
```powershell
curl http://localhost:8080/api/v1/registrations
```

### Get Specific Registration:
```powershell
curl http://localhost:8080/api/v1/registrations/{id}
```

### In Browser:
- http://localhost:8080/api/v1/registrations

---

## Quick View Commands

### See Recent Registrations (Pretty Format):
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT id, student_id, full_name, email, program, year_level, status, created_at FROM student_registrations ORDER BY created_at DESC LIMIT 10;"
```

### See Registration with Aggregated Data:
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT student_id, full_name, status, aggregated_profile FROM student_registrations WHERE aggregated_profile IS NOT NULL ORDER BY created_at DESC LIMIT 1;"
```

### Search by Student ID:
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT * FROM student_registrations WHERE student_id = '2024-AGG001';"
```

### Search by Program:
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT student_id, full_name, program, year_level FROM student_registrations WHERE program = 'Computer Science';"
```

---

## Understanding the Data

### Main Fields:
- **id** - Primary key (auto-generated)
- **student_id** - Unique student identifier
- **full_name** - Student's name
- **email** - Student email
- **program** - Academic program
- **year_level** - First Year, Second Year, etc.
- **status** - Registration status (e.g., PROFILE_COMPLETE)
- **message** - Processing messages
- **aggregated_profile** - JSON with all system responses
- **created_at** - When registered
- **updated_at** - Last update

### Aggregated Profile Contains:
```json
{
  "academicRecords": {...},
  "housing": {...} or "billing": {...},
  "library": {...},
  "aggregationTimestamp": "...",
  "responsesReceived": 3,
  "isComplete": true,
  "aggregationTimeMs": 1586
}
```

---

## Useful Queries

### View Only Complete Profiles:
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT student_id, full_name, status FROM student_registrations WHERE status = 'PROFILE_COMPLETE';"
```

### View Aggregation Times:
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT student_id, full_name, (aggregated_profile->>'aggregationTimeMs')::integer as ms FROM student_registrations WHERE aggregated_profile IS NOT NULL ORDER BY ms DESC;"
```

### View First Year Students:
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT student_id, full_name, program FROM student_registrations WHERE year_level = 'First Year';"
```

### Clear All Data (Careful!):
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "DELETE FROM student_registrations;"
```

---

## Export Data

### Export to CSV:
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "COPY student_registrations TO STDOUT WITH CSV HEADER;" > registrations.csv
```

### Export Specific Columns:
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "COPY (SELECT student_id, full_name, program, year_level, status FROM student_registrations) TO STDOUT WITH CSV HEADER;" > students.csv
```

---

## Common Issues

### If you get "relation does not exist":
The table hasn't been created yet. Register a student first, then Hibernate will create the table automatically.

### If you get "password authentication failed":
Double-check the credentials:
- Username: `rsu_user`
- Password: `rsu_password`
- Database: `rsu_registration`

### If you get "could not connect":
Make sure Docker container is running:
```powershell
docker ps
```

---

## Quick Copy-Paste Commands

### View Everything (Simple):
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT * FROM student_registrations;"
```

### View Latest 5 (Formatted):
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT id, student_id, full_name, program, year_level, status, created_at FROM student_registrations ORDER BY created_at DESC LIMIT 5;"
```

### Count by Program:
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT program, COUNT(*) as total FROM student_registrations GROUP BY program;"
```

### Count by Year Level:
```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT year_level, COUNT(*) as total FROM student_registrations GROUP BY year_level;"
```

---

**Try this command right now to see your data:**

```powershell
docker exec rsu_postgres psql -U rsu_user -d rsu_registration -c "SELECT student_id, full_name, program, year_level, status FROM student_registrations ORDER BY created_at DESC;"
```

This will show you all registered students! 📊
