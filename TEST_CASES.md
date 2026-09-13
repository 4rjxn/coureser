# Test Cases & Verification Report: Course Management System

## 1. Overview
This document specifies the test cases designed to validate system behavior, functional correctness, prerequisite validation, credit limit enforcement, and error handling for the **Course Management System**.

---

## 2. Test Suite Specifications

### Test Suite 1: Student Entity & Credit Limit Tests (`StudentTest.java`)

#### Test Case TC-01: Default Credit Ceiling & Remaining Credit Calculation
- **Input**: Create `Student` with name `"Alice"`, `maxCredits = 20`. Register for 2 courses (3 credits each).
- **Expected Output**:
  - `getRegisteredCredits()` returns `6`
  - `getRemainingCredits()` returns `14`
- **Result**: `PASS`

#### Test Case TC-02: Credit Ceiling Breach Validation
- **Input**: `Student` max credits = `10`. Attempt to register for courses totaling `12` credits.
- **Expected Output**: `canRegister(course)` returns `false`. `CreditLimitExceededException` thrown on service registration attempt.
- **Result**: `PASS`

---

### Test Suite 2: Course Prerequisite Validation (`CourseTest.java`)

#### Test Case TC-03: Prerequisite Enforcement (Missing Prerequisites)
- **Input**: Course `CS201` with prerequisite `"CS101"`. Student completed courses = `[]` (empty).
- **Expected Output**: `hasCompletedPrerequisites(CS201)` returns `false`. Registration fails with `PrerequisiteNotMetException`.
- **Result**: `PASS`

#### Test Case TC-04: Prerequisite Fulfillment (Satisfied Prerequisites)
- **Input**: Student completed `"CS101"`. Register for `CS201` requiring `"CS101"`.
- **Expected Output**: `hasCompletedPrerequisites(CS201)` returns `true`. Registration succeeds.
- **Result**: `PASS`

---

### Test Suite 3: Registration Logic & Capacity Constraints (`RegistrationServiceTest.java`)

#### Test Case TC-05: Capacity Exhaustion Test
- **Input**: Course `CS300` capacity = `1`, `enrolledCount = 1`. Student attempts registration.
- **Expected Output**: `isFull()` returns `true`. Throws `CourseFullException`.
- **Result**: `PASS`

#### Test Case TC-06: Duplicate Course Registration Test
- **Input**: Student registered for `CS101`. Student attempts to register for `CS101` again.
- **Expected Output**: Throws `DuplicateRegistrationException`.
- **Result**: `PASS`

---

### Test Suite 4: System Integration & End-to-End Execution (`AppTest.java`)

#### Test Case TC-07: Full Student Registration & Summary Lifecycle
- **Input**:
  1. Add Student `"S1001"`, Max Credits: `15`.
  2. Add Course `"MATH101"`, Credits: `4`, Capacity: `30`.
  3. Register `"S1001"` for `"MATH101"`.
- **Expected Output**:
  - Student registered credits = `4`.
  - Course enrolled count = `1`.
  - Registration record created with status `"REGISTERED"`.
- **Result**: `PASS`

---

## 3. Sample Inputs and Outputs Demonstration

### Sample Input 1: Course Registration JSON Payload (HTTP API)
```json
POST /api/register
Content-Type: application/json

{
  "studentId": 1001,
  "courseCode": "CS101"
}
```

### Sample Output 1: Successful Registration API Response
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "SUCCESS",
  "message": "Successfully registered student 1001 for course CS101.",
  "data": {
    "studentId": 1001,
    "courseCode": "CS101",
    "totalRegisteredCredits": 4,
    "remainingCredits": 16
  }
}
```

---

### Sample Input 2: Failed Registration Payload (Missing Prerequisite)
```json
POST /api/register
Content-Type: application/json

{
  "studentId": 1002,
  "courseCode": "CS301"
}
```

### Sample Output 2: Failure Response with Missing Prerequisites Detail
```json
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "status": "ERROR",
  "errorType": "PrerequisiteNotMetException",
  "message": "Cannot register for course CS301: Missing prerequisites: [CS201, MATH201]"
}
```

---

## 4. Test Execution Summary

Running `mvn test` produces clean execution across all unit & integration tests:

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.courser.AppTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.145 s
[INFO] Running com.courser.CourseTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.032 s
[INFO] Running com.courser.RegistrarTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s
[INFO] Running com.courser.RegistrationServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.089 s
[INFO] Running com.courser.RegistrationTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.021 s
[INFO] Running com.courser.StudentTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.045 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
