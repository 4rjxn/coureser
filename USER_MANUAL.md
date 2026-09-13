# User Manual: Course Management System (`courser`)

## 1. Introduction & Requirements

### 1.1 Prerequisites
Before building and running the application, ensure the following tools are installed on your system:
- **Java Development Kit (JDK)**: Version 21 or higher.
- **Apache Maven**: Version 3.8+ for build and dependency management.
- **SQLite3 JDBC Driver**: Included automatically via Maven dependencies (`org.xerial:sqlite-jdbc`).

---

## 2. Setup & Installation Instructions

### Step 1: Clone or Navigate to the Workspace Directory
```bash
cd /home/parzival/Projects/vi_cour
```

### Step 2: Build and Package the Application
Use Apache Maven to compile the Java source files, run unit tests, and create a executable JAR package:

```bash
mvn clean package
```

Upon successful compilation, Maven generates the standalone shaded JAR file:
```
target/courser-1.0-SNAPSHOT.jar
```

---

## 3. How to Run the System

The system can be launched in **CLI Interface Mode** or **Embedded Web API Server Mode**.

### 3.1 Running in CLI Mode (Default)
To run the interactive command-line interface:
```bash
java -jar target/courser-1.0-SNAPSHOT.jar
```
*or via Maven:*
```bash
mvn exec:java -Dexec.mainClass="com.courser.App"
```

### 3.2 Running the HTTP Web Server
To launch the REST Web Server backend on port `8080`:
```bash
java -cp target/courser-1.0-SNAPSHOT.jar com.courser.server.Server
```

---

## 4. User System Workflows & Features

### 4.1 Student Role Workflow
1. **Browse Courses**: View available course offerings, schedules, credit counts, and prerequisite requirements.
2. **Check Registration Eligibility**: Validate if current registered credits do not exceed max limit (default: 20 credits) and required prerequisites are fulfilled.
3. **Register Course**: Add course to active schedule.
4. **Drop Course**: Withdraw from an enrolled course before deadline.
5. **View Summary**: Access student enrollment summary and remaining credit allowances.

### 4.2 Registrar Role Workflow
1. **Create/Update Courses**: Add new courses to system catalog with capacity and prerequisite definitions.
2. **Manage Registrations**: Override or manage student registrations.
3. **Generate Reports**: Inspect total system registration statistics and course enrollment capacities.

---

## 5. Web API Reference (REST Endpoints)

| Method | Endpoint | Description | Request Body Example |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/login` | Authenticate user credentials | `{"username":"john_doe"}` |
| `GET` | `/api/courses` | Retrieve list of all available courses | N/A |
| `POST` | `/api/register` | Register student for a course | `{"studentId": 1, "courseId": 101}` |
| `DELETE`| `/api/drop` | Drop an enrolled course | `{"studentId": 1, "courseId": 101}` |
| `GET` | `/api/student/summary` | Fetch credit summary for student | Query Param: `?studentId=1` |

---

## 6. Troubleshooting & Diagnostics

- **Database Locks / File Not Found**: The application creates and updates `courser.db` automatically in the root working directory. Ensure write permissions exist.
- **Port Conflict (Port 8080 in use)**: Verify no other service is occupying port 8080 or kill active process using `fuser -k 8080/tcp`.
- **Prerequisite / Credit Errors**: Check exception messages displayed on CLI or HTTP API response status codes.
