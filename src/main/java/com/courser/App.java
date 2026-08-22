package com.courser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.courser.exception.RegistrationException;
import com.courser.model.Course;
import com.courser.model.Registrar;
import com.courser.model.Registration;
import com.courser.model.Student;
import com.courser.server.Server;
import com.courser.services.CourseServices;
import com.courser.services.StudentServices;
import com.courser.utils.Database;

public class App {
    public static void main(String[] args) throws IOException {
        Database.init();

        if (args.length > 0 && "--cli".equalsIgnoreCase(args[0])) {
            runCli();
        } else {
            int port = 5555;
            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException ignored) {
                }
            }
            System.out.println("Starting Course Management System on port " + port + "...");
            System.out.println("Open http://localhost:" + port + "/admin/dashboard in your browser.");
            Server.serve(port);
        }
    }

    public static void runCli() {
        Scanner scanner = new Scanner(System.in);
        Registrar registrar = new Registrar(1, "admin", "REG001", "System Registrar", "registrar@university.edu",
                "Academic Records");
        boolean isRunning = true;

        System.out.println("=================================================");
        System.out.println("  Course Management & Registration System (CLI)  ");
        System.out.println("=================================================");

        while (isRunning) {
            showMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleAddCourse(scanner, registrar);
                case "2" -> handleUpdateCourse(scanner, registrar);
                case "3" -> handleRemoveCourse(scanner, registrar);
                case "4" -> handleListCourses(registrar);
                case "5" -> handleAddStudent(scanner, registrar);
                case "6" -> handleSearchStudent(scanner, registrar);
                case "7" -> handleAddCompletedCourse(scanner);
                case "8" -> handleRegisterCourse(scanner, registrar);
                case "9" -> handleDropCourse(scanner, registrar);
                case "10" -> handleViewStudentRegistrations(scanner, registrar);
                case "11" -> handleViewAllRegistrations(registrar);
                case "q", "Q" -> {
                    isRunning = false;
                    System.out.println("Exiting system. Goodbye!");
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

    private static void showMenu() {
        System.out.print("""
                -------------------------------------------------
                Course Management:
                  1. Add New Course
                  2. Update Course
                  3. Remove Course
                  4. List & Search Courses
                Student Management:
                  5. Register New Student
                  6. Search Students
                  7. Record Completed Course (Prerequisite)
                Course Registration:
                  8. Register Student for Course (Enforces Prereq, Capacity & Credit Limit)
                  9. Drop Course for Student
                  10. View Student's Registered Courses
                  11. View All System Registrations
                Exit:
                  q. Quit
                -------------------------------------------------
                Select an option > """);
    }

    private static void handleAddCourse(Scanner scanner, Registrar registrar) {
        try {
            System.out.print("Enter course code (e.g. CS101) > ");
            String code = scanner.nextLine().trim();
            System.out.print("Enter course title > ");
            String title = scanner.nextLine().trim();
            System.out.print("Enter credits > ");
            int credits = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter instructor name > ");
            String instructor = scanner.nextLine().trim();
            System.out.print("Enter maximum capacity (default 30) > ");
            String capInput = scanner.nextLine().trim();
            int capacity = capInput.isEmpty() ? 30 : Integer.parseInt(capInput);
            System.out.print("Enter prerequisite course codes (comma-separated, or leave blank) > ");
            String prereqInput = scanner.nextLine().trim();
            List<String> prereqs = prereqInput.isEmpty() ? List.of()
                    : Arrays.stream(prereqInput.split(",")).map(String::trim).toList();

            Course course = new Course(code, title, credits, instructor, prereqs, capacity);
            registrar.addCourse(course);
            System.out.println("✓ Course " + code + " added successfully!");
        } catch (Exception e) {
            System.out.println("✗ Error adding course: " + e.getMessage());
        }
    }

    private static void handleUpdateCourse(Scanner scanner, Registrar registrar) {
        try {
            System.out.print("Enter course code to update > ");
            String originalCode = scanner.nextLine().trim();
            Course existing = CourseServices.getCourseByCode(originalCode);
            if (existing == null) {
                System.out.println("✗ Course not found.");
                return;
            }
            System.out.print("Enter new title [" + existing.getTitle() + "] > ");
            String title = scanner.nextLine().trim();
            if (title.isEmpty())
                title = existing.getTitle();

            System.out.print("Enter new credits [" + existing.getCredits() + "] > ");
            String creditsInput = scanner.nextLine().trim();
            int credits = creditsInput.isEmpty() ? existing.getCredits() : Integer.parseInt(creditsInput);

            System.out.print("Enter new instructor [" + existing.getInstructorName() + "] > ");
            String instructor = scanner.nextLine().trim();
            if (instructor.isEmpty())
                instructor = existing.getInstructorName();

            System.out.print("Enter new capacity [" + existing.getCapacity() + "] > ");
            String capInput = scanner.nextLine().trim();
            int capacity = capInput.isEmpty() ? existing.getCapacity() : Integer.parseInt(capInput);

            System.out.print("Enter prerequisite codes (comma-separated) > ");
            String prereqInput = scanner.nextLine().trim();
            List<String> prereqs = prereqInput.isEmpty() ? existing.getPrerequisitesList()
                    : Arrays.stream(prereqInput.split(",")).map(String::trim).toList();

            Course updated = new Course(originalCode, title, credits, instructor, prereqs, capacity);
            registrar.updateCourse(updated, originalCode);
            System.out.println("✓ Course " + originalCode + " updated successfully!");
        } catch (Exception e) {
            System.out.println("✗ Error updating course: " + e.getMessage());
        }
    }

    private static void handleRemoveCourse(Scanner scanner, Registrar registrar) {
        System.out.print("Enter course code to remove > ");
        String code = scanner.nextLine().trim();
        boolean removed = registrar.removeCourse(code);
        if (removed) {
            System.out.println("✓ Course " + code + " removed successfully.");
        } else {
            System.out.println("✗ Failed to remove course " + code + ".");
        }
    }

    private static void handleListCourses(Registrar registrar) {
        List<Course> courses = registrar.searchCourses("");
        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }
        System.out.println("\n--- Course Catalog ---");
        for (Course c : courses) {
            String[] prereqs = c.getPrerequisiteCourses();
            String prereqStr = (prereqs.length > 0) ? String.join(", ", prereqs) : "None";
            System.out.printf("[%s] %s | %d Credits | Instructor: %s | Seats: %d/%d | Prereqs: %s%n",
                    c.getCourseCode(), c.getTitle(), c.getCredits(), c.getInstructorName(),
                    c.getAvailableSeats(), c.getCapacity(), prereqStr);
        }
    }

    private static void handleAddStudent(Scanner scanner, Registrar registrar) {
        try {
            System.out.print("Enter student username > ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter student ID (e.g. STU101) > ");
            String studentId = scanner.nextLine().trim();
            System.out.print("Enter full name > ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter email > ");
            String email = scanner.nextLine().trim();
            System.out.print("Enter max credits per semester (default 20) > ");
            String credInput = scanner.nextLine().trim();
            int maxCredits = credInput.isEmpty() ? 20 : Integer.parseInt(credInput);

            Student student = new Student(username, studentId, name, email, maxCredits);
            registrar.registerStudent(student);
            System.out.println("✓ Student " + name + " (" + studentId + ") registered successfully!");
        } catch (Exception e) {
            System.out.println("✗ Error adding student: " + e.getMessage());
        }
    }

    private static void handleSearchStudent(Scanner scanner, Registrar registrar) {
        System.out.print("Enter search term (ID or Name) > ");
        String query = scanner.nextLine().trim();
        List<Student> students = registrar.searchStudents(query);
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        System.out.println("\n--- Students ---");
        for (Student s : students) {
            System.out.printf("[%s] %s (%s) | Email: %s | Max Credits: %d | Registered Credits: %d%n",
                    s.getStudentId(), s.getName(), s.getUserName(), s.getEmail(),
                    s.getMaxCredits(), s.getRegisteredCredits());
        }
    }

    private static void handleAddCompletedCourse(Scanner scanner) {
        System.out.print("Enter student ID > ");
        String studentId = scanner.nextLine().trim();
        System.out.print("Enter completed course code (e.g. CS101) > ");
        String courseCode = scanner.nextLine().trim();
        StudentServices.addCompletedCourse(studentId, courseCode);
        System.out.println("✓ Recorded " + courseCode + " as completed for student " + studentId + ".");
    }

    private static void handleRegisterCourse(Scanner scanner, Registrar registrar) {
        try {
            System.out.print("Enter student ID > ");
            String studentId = scanner.nextLine().trim();
            Student student = registrar.getStudent(studentId);
            if (student == null) {
                System.out.println("✗ Student not found.");
                return;
            }
            System.out.print("Enter course code to register > ");
            String courseCode = scanner.nextLine().trim();
            Course course = CourseServices.getCourseByCode(courseCode);
            if (course == null) {
                System.out.println("✗ Course not found.");
                return;
            }

            Registration reg = registrar.registerStudentForCourse(student, course);
            System.out.println("✓ SUCCESS! Registered " + student.getName() + " for " + course.getCourseCode() + " ("
                    + course.getTitle() + ").");
            System.out.println("  Registration Date: " + reg.getRegistrationDate());
            System.out.println(
                    "  Total Registered Credits: " + student.getRegisteredCredits() + " / " + student.getMaxCredits());
        } catch (RegistrationException e) {
            System.out.println("✗ REGISTRATION REJECTED: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private static void handleDropCourse(Scanner scanner, Registrar registrar) {
        System.out.print("Enter student ID > ");
        String studentId = scanner.nextLine().trim();
        Student student = registrar.getStudent(studentId);
        if (student == null) {
            System.out.println("✗ Student not found.");
            return;
        }
        System.out.print("Enter course code to drop > ");
        String courseCode = scanner.nextLine().trim();
        Course course = CourseServices.getCourseByCode(courseCode);
        if (course == null) {
            System.out.println("✗ Course not found.");
            return;
        }

        boolean dropped = registrar.dropStudentFromCourse(student, course);
        if (dropped) {
            System.out.println("✓ SUCCESS! Dropped course " + courseCode + " for student " + student.getName() + ".");
            System.out.println("  Updated Registered Credits: " + student.getRegisteredCredits() + " / "
                    + student.getMaxCredits());
        } else {
            System.out.println("✗ Failed to drop course. Student was not registered for this course.");
        }
    }

    private static void handleViewStudentRegistrations(Scanner scanner, Registrar registrar) {
        System.out.print("Enter student ID > ");
        String studentId = scanner.nextLine().trim();
        Student student = registrar.getStudent(studentId);
        if (student == null) {
            System.out.println("✗ Student not found.");
            return;
        }
        List<Course> courses = registrar.viewStudentCourses(studentId);
        System.out
                .println("\n--- Registered Courses for " + student.getName() + " (" + student.getStudentId() + ") ---");
        System.out.println("Max Allowed Credits: " + student.getMaxCredits());
        System.out.println("Total Registered Credits: " + student.getRegisteredCredits());
        System.out.println("Remaining Credit Capacity: " + student.getRemainingCredits());
        if (courses.isEmpty()) {
            System.out.println("No currently registered courses.");
        } else {
            for (Course c : courses) {
                System.out.printf(" - [%s] %s (%d credits, Instructor: %s)%n",
                        c.getCourseCode(), c.getTitle(), c.getCredits(), c.getInstructorName());
            }
        }
    }

    private static void handleViewAllRegistrations(Registrar registrar) {
        List<Registration> list = registrar.viewAllRegistrations();
        if (list.isEmpty()) {
            System.out.println("No active registrations found.");
            return;
        }
        System.out.println("\n--- Active System Registrations ---");
        for (Registration r : list) {
            System.out.printf("Reg #%d | Student: %s (%s) | Course: %s - %s (%d cr) | Date: %s%n",
                    r.getRegistrationId(), r.getStudent().getStudentId(), r.getStudent().getName(),
                    r.getCourse().getCourseCode(), r.getCourse().getTitle(), r.getCourse().getCredits(),
                    r.getRegistrationDate());
        }
    }
}
