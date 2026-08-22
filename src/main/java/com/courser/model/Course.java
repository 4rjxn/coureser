package com.courser.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Course in the Course Registration System.
 * Encapsulates course code, title, credits, instructor name, prerequisite course codes,
 * and capacity limit.
 */
public class Course {
    private static final int DEFAULT_CAPACITY = 30;

    private String courseCode;
    private String title;
    private int credits;
    private String instructorName;
    private final List<String> prerequisiteCourses = new ArrayList<>();
    private int capacity = DEFAULT_CAPACITY;
    private int enrolledCount = 0;

    public Course(String courseCode, String title, int credits, String instructorName,
                  String[] prerequisiteCourses) {
        this(courseCode, title, credits, instructorName,
                prerequisiteCourses != null ? List.of(prerequisiteCourses) : Collections.emptyList(),
                DEFAULT_CAPACITY, 0);
    }

    public Course(String courseCode, String title, int credits, String instructorName,
                  List<String> prerequisiteCourses) {
        this(courseCode, title, credits, instructorName, prerequisiteCourses, DEFAULT_CAPACITY, 0);
    }

    public Course(String courseCode, String title, int credits, String instructorName,
                  List<String> prerequisiteCourses, int capacity) {
        this(courseCode, title, credits, instructorName, prerequisiteCourses, capacity, 0);
    }

    public Course(String courseCode, String title, int credits, String instructorName,
                  List<String> prerequisiteCourses, int capacity, int enrolledCount) {
        setCourseCode(courseCode);
        setTitle(title);
        setCredits(credits);
        setInstructorName(instructorName);
        setCapacity(capacity);
        this.enrolledCount = Math.max(0, enrolledCount);
        if (prerequisiteCourses != null) {
            for (String prereq : prerequisiteCourses) {
                if (prereq != null && !prereq.isBlank()) {
                    this.prerequisiteCourses.add(prereq.trim().toUpperCase());
                }
            }
        }
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = Objects.requireNonNull(courseCode, "Course code cannot be null").trim().toUpperCase();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "Course title cannot be null").trim();
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be a positive integer.");
        }
        this.credits = credits;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = Objects.requireNonNull(instructorName, "Instructor name cannot be null").trim();
    }

    public String[] getPrerequisiteCourses() {
        return prerequisiteCourses.toArray(new String[0]);
    }

    public List<String> getPrerequisitesList() {
        return Collections.unmodifiableList(prerequisiteCourses);
    }

    public void addPrerequisite(String prerequisiteCode) {
        if (prerequisiteCode != null && !prerequisiteCode.isBlank()) {
            String formatted = prerequisiteCode.trim().toUpperCase();
            if (!prerequisiteCourses.contains(formatted)) {
                prerequisiteCourses.add(formatted);
            }
        }
    }

    public boolean removePrerequisite(String prerequisiteCode) {
        if (prerequisiteCode != null) {
            return prerequisiteCourses.remove(prerequisiteCode.trim().toUpperCase());
        }
        return false;
    }

    public boolean hasPrerequisite(String prerequisiteCode) {
        if (prerequisiteCode == null) return false;
        return prerequisiteCourses.contains(prerequisiteCode.trim().toUpperCase());
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        }
        this.capacity = capacity;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = Math.max(0, enrolledCount);
    }

    public int getAvailableSeats() {
        return Math.max(0, capacity - enrolledCount);
    }

    public boolean isFull() {
        return enrolledCount >= capacity;
    }

    public boolean enroll() {
        if (isFull()) {
            return false;
        }
        enrolledCount++;
        return true;
    }

    public boolean drop() {
        if (enrolledCount > 0) {
            enrolledCount--;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return courseCode != null && courseCode.equalsIgnoreCase(course.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode != null ? courseCode.toUpperCase() : "");
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseCode='" + courseCode + '\'' +
                ", title='" + title + '\'' +
                ", credits=" + credits +
                ", instructorName='" + instructorName + '\'' +
                ", prerequisites=" + prerequisiteCourses +
                ", capacity=" + capacity +
                ", enrolled=" + enrolledCount +
                '}';
    }
}
