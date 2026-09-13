# Source Code Overview: Course Management System

## 1. Directory & Package Structure

```
src/
├── main/
│   └── java/
│       └── com/
│           └── courser/
│               ├── App.java                   # Main Application Launcher & Command-line Interface
│               ├── dao/                       # Data Access Objects (SQLite Persistence)
│               │   ├── CourseDao.java         # Course Database Operations
│               │   ├── RegistrationDao.java   # Course Registration Persistence
│               │   └── UserDao.java           # User & Student Database Access
│               ├── exception/                 # Custom Domain Exceptions
│               │   ├── CourseFullException.java
│               │   ├── CreditLimitExceededException.java
│               │   ├── DuplicateRegistrationException.java
│               │   ├── PrerequisiteNotMetException.java
│               │   └── RegistrationException.java
│               ├── model/                     # Domain Object Model
│               │   ├── Course.java            # Course Entity
│               │   ├── Registrar.java         # Registrar User Entity
│               │   ├── Registration.java      # Registration Junction Entity
│               │   ├── Student.java           # Student User Entity
│               │   ├── Teacher.java           # Teacher User Entity
│               │   └── User.java              # Base User Entity
│               ├── server/                    # HTTP Web API Server
│               │   ├── Server.java            # Embedded HttpServer setup (Port 8080)
│               │   ├── ServerHandles.java     # Endpoint HTTP Request Handlers
│               │   └── ServerUtils.java       # HTTP JSON response formatting utilities
│               ├── services/                  # Business Logic Services
│               │   ├── CourseServices.java    # Catalog & Course Management Logic
│               │   ├── RegistrationService.java # Registration Validation & Processing
│               │   ├── StudentServices.java   # Student Record Logic
│               │   ├── SummaryService.java    # Analytical & Summary Reports
│               │   └── UserServices.java       # User Auth & Management Services
│               └── utils/                     # Database & Utility Helpers
│                   └── Database.java          # SQLite JDBC Driver & Table Initializer
└── test/
    └── java/
        └── com/
            └── courser/                       # JUnit 4 Test Suite
                ├── AppTest.java
                ├── CourseTest.java
                ├── RegistrarTest.java
                ├── RegistrationServiceTest.java
                ├── RegistrationTest.java
                └── StudentTest.java
```

---

## 2. Key Modules & Implementation Highlights

### 2.1 Object-Oriented Domain Layer (`com.courser.model`)

#### `User.java`
Base class providing core user fields (`userId`, `userName`, `name`, `email`) with validation against null/empty strings:
```java
public class User {
    private int userId;
    private String userName;
    private String name;
    private String email;

    public User(int userId, String userName, String name, String email) {
        this.userId = userId;
        this.userName = Objects.requireNonNull(userName, "Username cannot be null").trim();
        this.name = Objects.requireNonNull(name, "Name cannot be null").trim();
        this.email = Objects.requireNonNull(email, "Email cannot be null").trim();
    }
}
```

#### `Student.java`
Inherits from `User`. Encapsulates student registration constraints, prerequisite checks, and credit limits:
```java
public class Student extends User {
    public static final int DEFAULT_MAX_CREDITS = 20;
    private String studentId;
    private int maxCredits = DEFAULT_MAX_CREDITS;
    private final List<Course> registeredCourses = new ArrayList<>();
    private final Set<String> completedCourses = new HashSet<>();

    public boolean canRegister(Course course) {
        if (course == null) return false;
        if (isRegisteredFor(course)) return false;
        if (course.isFull()) return false;
        if (getRegisteredCredits() + course.getCredits() > maxCredits) return false;
        return hasCompletedPrerequisites(course);
    }
}
```

---

### 2.2 Exception Handling (`com.courser.exception`)

Custom domain exception hierarchy provides clean error diagnostics for business constraint violations:
- `RegistrationException`: Base exception class.
- `CourseFullException`: Thrown when attempting to register for a course with zero available seats.
- `CreditLimitExceededException`: Thrown when a registration exceeds the student's maximum credit limit (`maxCredits`).
- `DuplicateRegistrationException`: Thrown when a student re-registers for an already enrolled course.
- `PrerequisiteNotMetException`: Thrown when required prerequisites are missing from student's completed courses history.

```java
public class PrerequisiteNotMetException extends RegistrationException {
    private final List<String> missingPrerequisites;

    public PrerequisiteNotMetException(String courseCode, List<String> missingPrerequisites) {
        super("Cannot register for course " + courseCode + ": Missing prerequisites: " + missingPrerequisites);
        this.missingPrerequisites = missingPrerequisites;
    }
}
```

---

### 2.3 Registration Business Service (`RegistrationService.java`)

```java
public void registerStudentForCourse(Student student, Course course) throws RegistrationException {
    if (student.isRegisteredFor(course)) {
        throw new DuplicateRegistrationException("Student already enrolled in " + course.getCourseCode());
    }
    if (course.isFull()) {
        throw new CourseFullException("Course " + course.getCourseCode() + " is full.");
    }
    if (student.getRegisteredCredits() + course.getCredits() > student.getMaxCredits()) {
        throw new CreditLimitExceededException("Exceeds credit limit of " + student.getMaxCredits());
    }
    if (!student.hasCompletedPrerequisites(course)) {
        throw new PrerequisiteNotMetException(course.getCourseCode(), student.getMissingPrerequisites(course));
    }
    student.registerCourse(course);
    course.incrementEnrolledCount();
    registrationDao.createRegistration(student.getUserId(), course.getId());
}
```

---

### 2.4 Data Persistence (`Database.java` & DAOs)

SQLite connection handling with transactional reliability and schema initialization:
```java
public class Database {
    private static final String DB_URL = "jdbc:sqlite:courser.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
```

---

### 2.5 REST HTTP Web Server (`Server.java` & `ServerHandles.java`)

An embedded light HTTP web service handling RESTful client interactions using standard Java libraries (`com.sun.net.httpserver.HttpServer`):
- `POST /api/login`: User authentication endpoint.
- `GET /api/courses`: Lists available catalog courses.
- `POST /api/register`: Course registration endpoint.
- `GET /api/student/summary`: Generates student enrollment summary report.
