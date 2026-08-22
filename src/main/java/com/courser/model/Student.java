package com.courser.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a Student in the Course Management System.
 * Inherits user identity from User and encapsulates studentId, maximum credit limits,
 * registered courses, and completed course history for prerequisite verification.
 */
public class Student extends User {
    public static final int DEFAULT_MAX_CREDITS = 20;

    private String studentId;
    private int maxCredits = DEFAULT_MAX_CREDITS;
    private final List<Course> registeredCourses = new ArrayList<>();
    private final Set<String> completedCourses = new HashSet<>();

    public Student(int userId, String userName, String studentId, String name, String email,
                   int maxCredits, List<Course> registeredCourses) {
        super(userId, userName, name, email);
        setStudentId(studentId);
        setMaxCredits(maxCredits);
        if (registeredCourses != null) {
            this.registeredCourses.addAll(registeredCourses);
        }
    }

    public Student(int userId, String userName, String studentId, String name, String email,
                   int maxCredits, int[] registeredCourses) {
        super(userId, userName, name, email);
        setStudentId(studentId);
        setMaxCredits(maxCredits);
    }

    public Student(int userId, String userName, String studentId, String name, String email,
                   int maxCredits) {
        this(userId, userName, studentId, name, email, maxCredits, (List<Course>) null);
    }

    public Student(String userName, String studentId, String name, String email, int maxCredits) {
        this(0, userName, studentId, name, email, maxCredits, (List<Course>) null);
    }

    public Student(String userName, String studentId, String name, String email) {
        this(0, userName, studentId, name, email, DEFAULT_MAX_CREDITS, (List<Course>) null);
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = Objects.requireNonNull(studentId, "Student ID cannot be null").trim();
    }

    public int getMaxCredits() {
        return maxCredits;
    }

    public void setMaxCredits(int maxCredits) {
        if (maxCredits <= 0) {
            throw new IllegalArgumentException("Maximum credits must be greater than 0.");
        }
        this.maxCredits = maxCredits;
    }

    public List<Course> getRegisteredCourses() {
        return Collections.unmodifiableList(registeredCourses);
    }

    public void setRegisteredCourses(List<Course> courses) {
        this.registeredCourses.clear();
        if (courses != null) {
            this.registeredCourses.addAll(courses);
        }
    }

    public int getRegisteredCredits() {
        int total = 0;
        for (Course course : registeredCourses) {
            total += course.getCredits();
        }
        return total;
    }

    public int getRemainingCredits() {
        return Math.max(0, maxCredits - getRegisteredCredits());
    }

    public boolean isRegisteredFor(String courseCode) {
        if (courseCode == null) return false;
        for (Course c : registeredCourses) {
            if (c.getCourseCode().equalsIgnoreCase(courseCode.trim())) {
                return true;
            }
        }
        return false;
    }

    public boolean isRegisteredFor(Course course) {
        return course != null && isRegisteredFor(course.getCourseCode());
    }

    public Set<String> getCompletedCourses() {
        return Collections.unmodifiableSet(completedCourses);
    }

    public void addCompletedCourse(String courseCode) {
        if (courseCode != null && !courseCode.isBlank()) {
            completedCourses.add(courseCode.trim().toUpperCase());
        }
    }

    public void removeCompletedCourse(String courseCode) {
        if (courseCode != null) {
            completedCourses.remove(courseCode.trim().toUpperCase());
        }
    }

    public boolean hasCompletedCourse(String courseCode) {
        if (courseCode == null) return false;
        return completedCourses.contains(courseCode.trim().toUpperCase());
    }

    public boolean hasCompletedPrerequisites(Course course) {
        if (course == null) return false;
        List<String> prereqs = course.getPrerequisitesList();
        if (prereqs == null || prereqs.isEmpty()) {
            return true;
        }
        for (String prereq : prereqs) {
            if (!completedCourses.contains(prereq.toUpperCase())) {
                return false;
            }
        }
        return true;
    }

    public List<String> getMissingPrerequisites(Course course) {
        List<String> missing = new ArrayList<>();
        if (course != null) {
            for (String prereq : course.getPrerequisitesList()) {
                if (!completedCourses.contains(prereq.toUpperCase())) {
                    missing.add(prereq);
                }
            }
        }
        return missing;
    }

    public boolean canRegister(Course course) {
        if (course == null) return false;
        if (isRegisteredFor(course)) return false;
        if (course.isFull()) return false;
        if (getRegisteredCredits() + course.getCredits() > maxCredits) return false;
        return hasCompletedPrerequisites(course);
    }

    public boolean registerCourse(Course course) {
        if (course == null || isRegisteredFor(course)) {
            return false;
        }
        registeredCourses.add(course);
        return true;
    }

    public boolean dropCourse(String courseCode) {
        if (courseCode == null) return false;
        return registeredCourses.removeIf(c -> c.getCourseCode().equalsIgnoreCase(courseCode.trim()));
    }

    public boolean dropCourse(Course course) {
        if (course == null) return false;
        return dropCourse(course.getCourseCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return studentId != null && studentId.equalsIgnoreCase(student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId != null ? studentId.toLowerCase() : "");
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", maxCredits=" + maxCredits +
                ", registeredCredits=" + getRegisteredCredits() +
                ", registeredCoursesCount=" + registeredCourses.size() +
                '}';
    }
}
